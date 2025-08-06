package com.dti.drone_delivery_simulator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<List<Order>> tryAllocate(Order order, List<Order> pendingOrdersSnapshot) {
        return this.droneRepository.findByState(DroneState.IDLE).stream()
                .filter(drone -> this.isCompatible(drone, order))
                .findFirst()
                .map(drone -> {
                    List<Order> ordersToDeliver = new ArrayList<>();
                    ordersToDeliver.add(order);
                    combineWithPendingOrders(drone, ordersToDeliver, pendingOrdersSnapshot);
                    return ordersToDeliver;
                });
    }

    private void combineWithPendingOrders(Drone drone, List<Order> currentOrders, List<Order> pendingOrdersSnapshot) {
        for (Order pendingOrder : pendingOrdersSnapshot) {
            if (currentOrders.contains(pendingOrder)) continue;

            List<Order> potentialOrders = new ArrayList<>(currentOrders);
            potentialOrders.add(pendingOrder);

            double totalPayload = potentialOrders.stream().mapToDouble(Order::getPayloadKg).sum();
            double routeDistance = routeService.calculateRouteDistance(potentialOrders, drone.getPositionX(), drone.getPositionY());

            if (totalPayload <= drone.getMaxPayloadKg() && routeDistance <= drone.getMaxRangeKm()) {
                currentOrders.add(pendingOrder);
            }
        }
    }

    private boolean isCompatible(Drone drone, Order order) {
        List<Order> combinedOrders = new ArrayList<>(drone.getOrders());
        combinedOrders.add(order);

        double totalPayload = combinedOrders.stream()
                .mapToDouble(Order::getPayloadKg)
                .sum();

        double routeDistance = routeService.calculateRouteDistance(combinedOrders, drone.getPositionX(), drone.getPositionY());

        return totalPayload <= drone.getMaxPayloadKg() && routeDistance <= drone.getMaxRangeKm();
    }
}