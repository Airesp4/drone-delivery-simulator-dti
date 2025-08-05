package com.dti.drone_delivery_simulator.service;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final InMemoryDroneRepository droneRepository;
    private final InMemoryOrderRepository orderRepository;

    public void tryAllocateOrder(Order order) {
        Drone availableDrone = this.droneRepository.findByState(DroneState.IDLE).stream()
            .filter(drone -> this.isCompatible(drone, order))
            .findFirst()
            .orElse(null);

        if (availableDrone != null) {
            this.droneRepository.addOrderToDrone(availableDrone.getId(), order);
            this.simulateDelivery(availableDrone, order);
        } else {
            log.warn("⚠️ Nenhum drone disponível para o pedido {}. Ele ficará aguardando alocação.", order.getId());
        }
    }

    private void simulateDelivery(Drone drone, Order order) {
        new Thread(() -> {
            try {
                long droneId = drone.getId();
                double distance = this.calculateDistance(
                    drone.getPositionX(), drone.getPositionY(),
                    order.getClientPositionX(), order.getClientPositionY()
                );

                log.info("🛫 Drone {} iniciando entrega do pedido {}...", droneId, order.getId());

                this.droneRepository.advanceDroneState(droneId);
                log.info("📦 Drone {} está carregando...", droneId);
                this.sleepSeconds(1);

                this.droneRepository.advanceDroneState(droneId);
                log.info("✈️ Drone {} em voo para entregar pedido {}...", droneId, order.getId());
                this.sleepSeconds((long) distance);

                this.droneRepository.advanceDroneState(droneId);
                log.info("📍 Drone {} entregando pedido {}...", droneId, order.getId());
                this.sleepSeconds(1);

                order.setDelivered(true);
                this.orderRepository.update(order);
                log.info("📬 Pedido {} marcado como entregue.", order.getId());
                
                this.droneRepository.advanceDroneState(droneId);
                log.info("↩️ Drone {} retornando à base...", droneId);
                this.sleepSeconds((long) distance);

                this.droneRepository.removeOrderFromDrone(droneId, order.getId());
                this.droneRepository.advanceDroneState(droneId);
                log.info("✅ Drone {} voltou à base e está disponível (IDLE). Pedido {} finalizado.", droneId, order.getId());
                log.info("📦 Entrega do pedido {} concluída com sucesso pelo drone {}. Tempo total: ~{}s", order.getId(), droneId, 2 + (long)(distance * 2));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("🚨 Simulação interrompida para o pedido {}: {}", order.getId(), e.getMessage());
            } catch (Exception e) {
                log.error("❌ Erro na simulação de entrega do pedido {}: {}", order.getId(), e.getMessage());
            }
        }).start();
    }

    private boolean isCompatible(Drone drone, Order order) {
        double distance = this.calculateDistance(
            drone.getPositionX(), drone.getPositionY(),
            order.getClientPositionX(), order.getClientPositionY()
        );

        boolean payloadOK = order.getPayloadKg() <= drone.getMaxPayloadKg();
        boolean rangeOK = (distance * 2) <= drone.getMaxRangeKm();

        return payloadOK && rangeOK;
    }

    private double calculateDistance(int x1, int y1, int x2, int y2) {
        int deltaX = x2 - x1;
        int deltaY = y2 - y1;

        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    private void sleepSeconds(long seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
    }
}