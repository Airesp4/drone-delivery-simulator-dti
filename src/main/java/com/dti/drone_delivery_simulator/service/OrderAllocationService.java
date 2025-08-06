package com.dti.drone_delivery_simulator.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.enums.OrderState;
import com.dti.drone_delivery_simulator.model.Drone;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.repository.InMemoryDroneRepository;
import com.dti.drone_delivery_simulator.repository.InMemoryOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderAllocationService {

    private final InMemoryOrderRepository orderRepository;
    private final InMemoryDroneRepository droneRepository;
    private final RouteService routeService;

    public List<Drone> allocatePendingOrders() {
        List<Order> pendingOrders = this.orderRepository.findPendingOrders();
        List<Drone> availableDrones = this.droneRepository.findByState(DroneState.IDLE);

        for (Drone drone : availableDrones) {
            List<Order> allocatedToThisDrone = new ArrayList<>();

            for (Order order : pendingOrders) {
                if (this.isCompatible(drone, order)) {
                    this.droneRepository.addOrderToDrone(drone.getId(), order);
                    
                    order.setState(OrderState.ALLOCATED);
                    this.orderRepository.update(order);
                    allocatedToThisDrone.add(order);
                }
            }
            pendingOrders.removeAll(allocatedToThisDrone);
        }
        return availableDrones;
    }

    private boolean isCompatible(Drone drone, Order order) {
        return hasAvailablePayload(drone, order) && hasSufficientRange(drone, order);
    }

    private boolean hasAvailablePayload(Drone drone, Order order) {
        double currentPayload = drone.getOrders().stream()
                .mapToDouble(Order::getPayloadKg)
                .sum();

        double totalPayload = currentPayload + order.getPayloadKg();
        return totalPayload <= drone.getMaxPayloadKg();
    }

    private boolean hasSufficientRange(Drone drone, Order order) {
        List<Order> simulatedOrders = new ArrayList<>(drone.getOrders());
        simulatedOrders.add(order);

        double totalDistance = routeService.calculateRouteDistance(simulatedOrders, 0, 0);
        return totalDistance <= drone.getMaxRangeKm();
    }
}