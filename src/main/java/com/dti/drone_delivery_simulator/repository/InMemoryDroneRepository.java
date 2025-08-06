package com.dti.drone_delivery_simulator.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.model.Drone;
import com.dti.drone_delivery_simulator.model.Order;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InMemoryDroneRepository implements DroneRepository{

    private final Map<Long, Drone> drones;

    @PostConstruct
    public void init() {
        drones.put(1L, new Drone(1L, 10.0, 20.0, DroneState.IDLE, 0, 0, new ArrayList<>()));
    }

    @Override
    public List<Drone> findAll() {
        return new ArrayList<>(drones.values());
    }

    @Override
    public Optional<Drone> findById(Long id) {
        return Optional.ofNullable(drones.get(id));
    }

    @Override
    public List<Drone> findByState(DroneState state) {
        return drones.values().stream()
            .filter(d -> d.getStatus() == state)
            .collect(Collectors.toList());
    }

    @Override
    public Drone addOrderToDrone(Long droneId, Order order) {
        Drone drone = this.findById(droneId)
                            .orElseThrow(() -> new IllegalArgumentException("Drone not found."));

        if (drone.getStatus() != DroneState.IDLE) {
            throw new IllegalStateException("Drone not available");
        }

        drone.getOrders().add(order);
        return drone;
    }

    @Override
    public Drone removeOrderFromDrone(Long droneId, Long orderId) {
        Drone drone = this.findById(droneId)
                        .orElseThrow(() -> new IllegalArgumentException("Drone not found."));

        boolean removed = drone.getOrders().removeIf(order -> order.getId().equals(orderId));

        if (!removed) {
            throw new IllegalArgumentException("Order not found on drone.");
        }

        return drone;
    }

    @Override
    public Drone advanceDroneState(Long droneId) {
        Drone drone = this.findById(droneId)
                        .orElseThrow(() -> new IllegalArgumentException("Drone not found."));

        drone.setStatus(drone.getStatus().next());
        return drone;
    }
}
