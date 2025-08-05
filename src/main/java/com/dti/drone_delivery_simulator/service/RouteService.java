package com.dti.drone_delivery_simulator.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dti.drone_delivery_simulator.model.Order;

@Service
public class RouteService {
    
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

    public List<Order> getOptimizedOrderList(List<Order> orders) {
        return orders.stream()
            .sorted(Comparator.comparingDouble(order ->
                calculateDistance(0, 0, order.getClientPositionX(), order.getClientPositionY())))
            .toList();
    }
}
