package com.dti.drone_delivery_simulator.model;

import java.util.ArrayList;
import java.util.List;

import com.dti.drone_delivery_simulator.enums.DroneState;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drone {
    
    private Long id;
    private double maxPayloadKg;
    private double maxRangeKm;

    private DroneState status = DroneState.IDLE;

    private int positionX = 0;
    private int positionY = 0;

    private List<Order> orders = new ArrayList<>();
}
