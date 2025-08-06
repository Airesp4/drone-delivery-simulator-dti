package com.dti.drone_delivery_simulator.model;

import com.dti.drone_delivery_simulator.enums.OrderPriority;
import com.dti.drone_delivery_simulator.enums.OrderState;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Order {
    
    private Long id;
    private int clientPositionX;
    private int clientPositionY;
    private double payloadKg;
    private OrderPriority priority;
    private OrderState state = OrderState.PENDING;
}
