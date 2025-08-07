package com.dti.drone_delivery_simulator.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.enums.OrderState;
import com.dti.drone_delivery_simulator.enums.RouteStatus;
import com.dti.drone_delivery_simulator.model.Drone;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.model.Route;
import com.dti.drone_delivery_simulator.repository.InMemoryDroneRepository;
import com.dti.drone_delivery_simulator.repository.InMemoryOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final InMemoryDroneRepository droneRepository;
    private final InMemoryOrderRepository orderRepository;
    private final RouteService routeService;
    private final OrderAllocationService allocationService;

    public void processOrders() {
        List<Drone> drones = this.allocationService.allocatePendingOrders();
        List<Order> pendingOrders = this.orderRepository.findPendingOrders();

        if (drones.isEmpty() && !pendingOrders.isEmpty()) {
            log.info("Nenhum drone disponível para alocar {} pedidos pendentes.", pendingOrders.size());
            return;
        }

        for (Drone drone : drones) {
            List<Order> ordersToDeliver = drone.getOrders();
            if (!ordersToDeliver.isEmpty()) {
                log.info("Drone {} alocado com {} pedido(s) para entrega.", drone.getId(), ordersToDeliver.size());
                this.simulateDelivery(drone.getId(), ordersToDeliver);
            }
        }
    }

    private void simulateDelivery(Long droneId, List<Order> orders) {
        new Thread(() -> {
            try {
                Drone drone = droneRepository.findById(droneId)
                    .orElseThrow(() -> new IllegalArgumentException("Drone não encontrado para entrega"));

                droneRepository.advanceDroneState(drone.getId());
                log.info("Drone {} está carregando os pedidos...", drone.getId());
                sleepSeconds(1);

                List<Order> optimizedOrders = routeService.getOptimizedRoute(orders);

                Route route = routeService.createRoute(drone, optimizedOrders);
                routeService.updateStatusRoute(route.getId(), RouteStatus.PLANNED);

                int currentX = drone.getPositionX();
                int currentY = drone.getPositionY();

                droneRepository.advanceDroneState(droneId);

                for (Order order : optimizedOrders) {
                    double travelDistance = routeService.calculateDistance(
                        currentX, currentY, order.getClientPositionX(), order.getClientPositionY());

                    log.info("Drone {} se deslocando de ({}, {}) para ({}, {})",
                        droneId, currentX, currentY, order.getClientPositionX(), order.getClientPositionY());

                    order.setState(OrderState.ON_ROUTE);
                    this.orderRepository.update(order);
                    routeService.updateStatusRoute(route.getId(), RouteStatus.IN_PROGRESS);

                    drone.setStatus(DroneState.IN_FLIGHT);
                    log.info("Drone {} em voo para entregar pedido {}...", droneId, order.getId());
                    sleepSeconds((long) travelDistance);

                    drone.setPositionX(order.getClientPositionX());
                    drone.setPositionY(order.getClientPositionY());

                    drone.setStatus(DroneState.DELIVERING);
                    log.info("Drone {} entregando pedido {} no destino ({}, {}).", droneId, order.getId(), drone.getPositionX(), drone.getPositionY());
                    sleepSeconds(1);

                    order.setState(OrderState.DELIVERED);
                    this.orderRepository.update(order);
                    log.info("Pedido {} entregue com sucesso!", order.getId());

                    droneRepository.removeOrderFromDrone(droneId, order.getId());

                    currentX = order.getClientPositionX();
                    currentY = order.getClientPositionY();
                }
                
                droneRepository.advanceDroneState(droneId);
                
                routeService.updateStatusRoute(route.getId(), RouteStatus.COMPLETED);

                double returnDistance = routeService.calculateDistance(currentX, currentY, 0, 0);
                log.info("Drone {} retornando à base.", droneId);
                sleepSeconds((long) returnDistance);

                droneRepository.advanceDroneState(droneId);
                drone.setPositionX(0);
                drone.setPositionY(0);
                log.info("Drone {} voltou à base e está disponível (IDLE). Entrega concluída.", droneId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Simulação interrompida para o drone {}: {}", droneId, e.getMessage());
            } catch (Exception e) {
                log.error("Erro inesperado durante a entrega com drone {}: {}", droneId, e.getMessage(), e);
            }
        }).start();
    }

    @Scheduled(fixedRate = 10000)
    private void checkPendingOrders() {
        log.debug("Verificando novos pedidos a cada 10 segundos...");
        this.processOrders();
    }

    private void sleepSeconds(long seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
    }
}