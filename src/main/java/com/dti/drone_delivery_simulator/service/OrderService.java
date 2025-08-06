package com.dti.drone_delivery_simulator.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.dti.drone_delivery_simulator.dto.OrderRequestDTO;
import com.dti.drone_delivery_simulator.enums.OrderState;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.repository.InMemoryOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final InMemoryOrderRepository orderRepository;
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Order createOrder(OrderRequestDTO dto) {
        if (dto.payloadKg() <= 0) {
            throw new IllegalArgumentException("Peso deve ser maior que zero.");
        }
        
        Order order = new Order(
            idGenerator.getAndIncrement(),
            dto.clientPositionX(),
            dto.clientPositionY(),
            dto.payloadKg(),
            dto.priority(),
            OrderState.PENDING
        );
    
        return this.orderRepository.save(order);
    }

    public List<Order> findAll(){
        return this.orderRepository.findAll();
    }
}