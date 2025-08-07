package com.dti.drone_delivery_simulator.dto;

public record StatisticsResponseDTO(
        int totalRoutes,
        double averageOrdersPerRoute,
        int totalOrdersCompleted,
        int dronesAvailable
) {
}
