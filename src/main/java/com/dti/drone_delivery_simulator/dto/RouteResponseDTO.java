package com.dti.drone_delivery_simulator.dto;

import java.util.List;

import com.dti.drone_delivery_simulator.enums.RouteStatus;

public record RouteResponseDTO(
    Long id,
    DroneSummaryDTO drone,
    double totalDistanceKm,
    RouteStatus status,
    List<OrderSummaryDTO> orders
) {
    
}
