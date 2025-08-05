package com.dti.drone_delivery_simulator.model;

import java.util.List;

import com.dti.drone_delivery_simulator.enums.RouteStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    
    private Long id;
    private Long droneId;
    private List<Order> orders;
    private double totalDistanceKm;
    private RouteStatus status;
}
