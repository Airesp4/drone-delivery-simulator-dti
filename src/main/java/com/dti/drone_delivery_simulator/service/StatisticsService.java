package com.dti.drone_delivery_simulator.service;
import org.springframework.stereotype.Service;

import com.dti.drone_delivery_simulator.dto.StatisticsResponseDTO;
import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.enums.OrderState;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final RouteService routeService;
    private final OrderService orderService;
    private final DroneService droneService;

    public StatisticsResponseDTO statisticsCalculation() {
        var allRoutes = routeService.findAllRoutes();
        var totalRoutes = allRoutes.size();

        var averageOrdersPerRoute = allRoutes.stream()
                .mapToInt(route -> route.orders().size())
                .average()
                .orElse(0.0);

        var totalOrdersCompleted = orderService.findAll().stream()
                .filter(order -> order.getState() == OrderState.DELIVERED)
                .count();

        var dronesAvailable = droneService.getAllDrones().stream()
                .filter(drone -> drone.status() == DroneState.IDLE)
                .count();

        return new StatisticsResponseDTO(
                totalRoutes,
                averageOrdersPerRoute,
                (int) totalOrdersCompleted,
                (int) dronesAvailable
        );
    }
}