package com.dti.drone_delivery_simulator.dto;

import com.dti.drone_delivery_simulator.enums.DroneState;

public record DroneStatusResponseDTO(Long id, double maxPayloadKg, double maxRangeKm, DroneState status) {
    
}
