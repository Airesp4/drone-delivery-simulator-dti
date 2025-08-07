package com.dti.drone_delivery_simulator.dto;

import com.dti.drone_delivery_simulator.enums.OrderPriority;

public record OrderSummaryDTO(
    Long id, 
    int clientPositionX, 
    int clientPositionY, 
    double payloadKg, 
    OrderPriority priority
) {
    
}