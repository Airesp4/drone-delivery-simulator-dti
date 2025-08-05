package com.dti.drone_delivery_simulator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;

import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.model.Drone;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.repository.InMemoryDroneRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final InMemoryDroneRepository droneRepository;
    private final RouteService routeService;

    public Optional<List<Order>> tryAllocate(Order order, PriorityQueue<Order> pendingOrders) {
        return this.droneRepository.findByState(DroneState.IDLE).stream()
                .filter(drone -> this.isCompatible(drone, order))
                .findFirst()
                .map(drone -> {
                    List<Order> ordersToDeliver = new ArrayList<>();
                    ordersToDeliver.add(order);
                    combineWithPendingOrders(drone, ordersToDeliver, pendingOrders);
                    return ordersToDeliver;
                });
    }

    private void combineWithPendingOrders(Drone drone, List<Order> currentOrders, PriorityQueue<Order> pendingOrders) {
        List<Order> remainingPendingOrders = new ArrayList<>(pendingOrders);
        List<Order> ordersToRemove = new ArrayList<>();

        for (Order pendingOrder : remainingPendingOrders) {
            List<Order> potentialOrders = new ArrayList<>(currentOrders);
            potentialOrders.add(pendingOrder);

            double currentPayload = potentialOrders.stream().mapToDouble(Order::getPayloadKg).sum();
            double currentDistance = routeService.calculateRouteDistance(potentialOrders, drone.getPositionX(), drone.getPositionY());

            if (currentPayload <= drone.getMaxPayloadKg() && currentDistance <= drone.getMaxRangeKm()) {
                currentOrders.add(pendingOrder);
                ordersToRemove.add(pendingOrder);
            }
        }
        pendingOrders.removeAll(ordersToRemove);
    }

    private boolean isCompatible(Drone drone, Order order) {
        List<Order> combinedOrders = new ArrayList<>(drone.getOrders());
        combinedOrders.add(order);

        double totalPayload = combinedOrders.stream()
                .mapToDouble(Order::getPayloadKg)
                .sum();

        double routeDistance = routeService.calculateRouteDistance(combinedOrders, drone.getPositionX(), drone.getPositionY());

        boolean payloadOk = totalPayload <= drone.getMaxPayloadKg();
        boolean rangeOk = routeDistance <= drone.getMaxRangeKm();

        return payloadOk && rangeOk;
    }
}