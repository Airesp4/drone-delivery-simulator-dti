package com.dti.drone_delivery_simulator.repository;

import java.util.List;

import com.dti.drone_delivery_simulator.model.Order;

public interface OrderRepository {
    Order save(Order order);
    List<Order> findAll();
}
