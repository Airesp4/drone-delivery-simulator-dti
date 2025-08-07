package com.dti.drone_delivery_simulator.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dti.drone_delivery_simulator.dto.DroneSummaryDTO;
import com.dti.drone_delivery_simulator.dto.OrderSummaryDTO;
import com.dti.drone_delivery_simulator.dto.RouteResponseDTO;
import com.dti.drone_delivery_simulator.enums.RouteStatus;
import com.dti.drone_delivery_simulator.model.Drone;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.model.Route;
import com.dti.drone_delivery_simulator.repository.InMemoryRouteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteService {
    
    private final InMemoryRouteRepository routeRepository;

    public Route createRoute(Drone drone, List<Order> orders) {
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("Drone must have at least one order to create a route.");
        }

        Route route = new Route();
        route.setDrone(drone);
        route.setOrders(orders);
        route.setTotalDistanceKm(calculateRouteDistance(orders, drone.getPositionX(), drone.getPositionY()));

        return routeRepository.save(route);
    }
    
    public Route updateStatusRoute(Long routeId, RouteStatus status) {
        return routeRepository.updateStatus(routeId, status);
    }

    public List<RouteResponseDTO> findAllRoutes(){
        return routeRepository.findAll().stream()
        .map(route -> {

            DroneSummaryDTO droneDTO = new DroneSummaryDTO(
                route.getDrone().getId(),
                route.getDrone().getMaxPayloadKg(),
                route.getDrone().getMaxRangeKm()
            );

            List<OrderSummaryDTO> ordersDTO = route.getOrders().stream()
                .map(order -> new OrderSummaryDTO(
                    order.getId(),
                    order.getClientPositionX(),
                    order.getClientPositionY(),
                    order.getPayloadKg(),
                    order.getPriority()
                ))
                .toList();

            return new RouteResponseDTO(
                route.getId(),
                droneDTO,
                route.getTotalDistanceKm(),
                route.getStatus(),
                ordersDTO
            );
        })
        .toList();
    }

    public double calculateRouteDistance(List<Order> orders, int startX, int startY) {
        if (orders.isEmpty()) return 0;

        orders = orders.stream()
                .sorted(Comparator.comparingDouble(order ->
                        calculateDistance(startX, startY, order.getClientPositionX(), order.getClientPositionY())))
                .toList();
        
        double totalDistance = 0;
        int previousX = startX;
        int previousY = startY;

        for (Order order : orders) {
            totalDistance += calculateDistance(previousX, previousY, order.getClientPositionX(), order.getClientPositionY());
            previousX = order.getClientPositionX();
            previousY = order.getClientPositionY();
        }
        
        totalDistance += calculateDistance(previousX, previousY, 0, 0);

        return totalDistance;
    }

    public double calculateDistance(int x1, int y1, int x2, int y2) {
        int deltaX = x2 - x1;
        int deltaY = y2 - y1;

        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public List<Order> getOptimizedRoute(List<Order> orders) {
        int currentX = 0;
        int currentY = 0;

        List<Order> sortedOrders = new ArrayList<>();
        List<Order> remainingOrders = new ArrayList<>(orders);

        while (!remainingOrders.isEmpty()) {
            Order nextOrder = null;
            double minDistance = Double.MAX_VALUE;

            for (Order order : remainingOrders) {
                double distance = calculateDistance(currentX, currentY, order.getClientPositionX(), order.getClientPositionY());
                if (distance < minDistance) {
                    minDistance = distance;
                    nextOrder = order;
                }
            }

            if (nextOrder != null) {
                sortedOrders.add(nextOrder);
                currentX = nextOrder.getClientPositionX();
                currentY = nextOrder.getClientPositionY();
                remainingOrders.remove(nextOrder);
            }
        }

        return sortedOrders;
    }
}
