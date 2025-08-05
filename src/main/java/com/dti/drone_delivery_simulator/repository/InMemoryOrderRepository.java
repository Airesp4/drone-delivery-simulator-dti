package com.dti.drone_delivery_simulator.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.dti.drone_delivery_simulator.model.Order;

@Repository
public class InMemoryOrderRepository implements OrderRepository{

    private final List<Order> orders = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Order save(Order order) {
        orders.add(order);
        return order;
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(orders);
    }

    @Override
    public Order update(Order updatedOrder) {
        synchronized (orders) {
            orders.removeIf(o -> o.getId().equals(updatedOrder.getId()));
            orders.add(updatedOrder);
        }
        return updatedOrder;
    }
}
