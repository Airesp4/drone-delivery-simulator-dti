package com.dti.drone_delivery_simulator.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.model.Drone;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.repository.InMemoryDroneRepository;
import com.dti.drone_delivery_simulator.repository.InMemoryOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);
    private final PriorityQueue<Order> pendingOrders = new PriorityQueue<>(
        Comparator.comparing(Order::getPriority).reversed()
    ); 

    private final InMemoryDroneRepository droneRepository;
    private final InMemoryOrderRepository orderRepository;
    private final RouteService routeService;
    private final AllocationService allocationService;

    public void processOrder(Order order) {
        synchronized (pendingOrders) {
            List<Order> pendingOrdersSnapshot = new ArrayList<>(pendingOrders);

            allocationService.tryAllocate(order, pendingOrdersSnapshot).ifPresentOrElse(
                ordersToDeliver -> {
                    log.info("✔️ Pedido {} alocado para drone com {} pedidos adicionais.", order.getId(), ordersToDeliver.size() - 1);

                    ordersToDeliver.stream()
                        .filter(o -> !o.equals(order))
                        .forEach(pendingOrders::remove);

                    Drone drone = droneRepository.findByState(DroneState.IDLE).get(0);
                    ordersToDeliver.forEach(o -> this.droneRepository.addOrderToDrone(drone.getId(), o));
                    this.simulateDelivery(drone, ordersToDeliver);
                },
                () -> {
                    if (!pendingOrders.contains(order)) {
                        pendingOrders.add(order);
                        log.warn("⚠️ Nenhum drone disponível para o pedido {}. Pedido adicionado à fila de pendentes.", order.getId());
                    }
                }
            );
        }
    }

    private void simulateDelivery(Drone drone, List<Order> orders) {
        new Thread(() -> {
            try {
                long droneId = drone.getId();

                this.droneRepository.advanceDroneState(droneId);
                log.info("📦 Drone {} está carregando os pedidos...", droneId);
                this.sleepSeconds(1);

                List<Order> optimizedOrders = this.routeService.getOptimizedOrderList(orders);

                int currentX = drone.getPositionX();
                int currentY = drone.getPositionY();

                for (Order o : optimizedOrders) {
                    double travelDistance = this.routeService.calculateDistance(
                        currentX, currentY, o.getClientPositionX(), o.getClientPositionY()
                    );

                    this.droneRepository.advanceDroneState(droneId);
                    log.info("✈️ Drone {} em voo para entregar pedido {}...", droneId, o.getId());
                    this.sleepSeconds((long) travelDistance);

                    drone.setPositionX(o.getClientPositionX());
                    drone.setPositionY(o.getClientPositionY());

                    this.droneRepository.advanceDroneState(droneId);
                    log.info("📍 Drone {} entregando pedido {}...", droneId, o.getId());
                    this.sleepSeconds(1);

                    o.setDelivered(true);
                    this.orderRepository.update(o);
                    log.info("📬 Pedido {} marcado como entregue.", o.getId());

                    this.droneRepository.removeOrderFromDrone(droneId, o.getId());

                    currentX = o.getClientPositionX();
                    currentY = o.getClientPositionY();
                }

                double returnDistance = this.routeService.calculateDistance(currentX, currentY, 0, 0);

                this.droneRepository.advanceDroneState(droneId);
                log.info("↩️ Drone {} retornando à base...", droneId);
                this.sleepSeconds((long) returnDistance);

                this.droneRepository.advanceDroneState(droneId);
                drone.setPositionX(0);
                drone.setPositionY(0);

                log.info("✅ Drone {} voltou à base e está disponível (IDLE). Entrega de {} pedidos finalizada.", droneId, orders.size());

                this.checkPendingQueue();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("🚨 Simulação interrompida para o drone {}: {}", drone.getId(), e.getMessage());
            }
        }).start();
    }

    @Scheduled(fixedRate = 10000)
    private void checkPendingQueue() {
        log.info("🔄 Checando pedidos pendentes...");
        synchronized (pendingOrders) {
            if (pendingOrders.isEmpty()) {
                return;
            }

            Order nextOrder = pendingOrders.peek();
            if (nextOrder != null) {
                List<Order> pendingOrdersSnapshot = new ArrayList<>(pendingOrders);

                allocationService.tryAllocate(nextOrder, pendingOrdersSnapshot).ifPresent(
                    ordersToDeliver -> {
                        ordersToDeliver.forEach(pendingOrders::remove);

                        Drone drone = droneRepository.findByState(DroneState.IDLE).get(0);
                        ordersToDeliver.forEach(o -> this.droneRepository.addOrderToDrone(drone.getId(), o));
                        this.simulateDelivery(drone, ordersToDeliver);
                    }
                );
            }
        }
    }

    private void sleepSeconds(long seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
    }
}