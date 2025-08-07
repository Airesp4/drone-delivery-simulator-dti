package com.dti.drone_delivery_simulator.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.dti.drone_delivery_simulator.dto.OrderRequestDTO;
import com.dti.drone_delivery_simulator.enums.OrderState;
import com.dti.drone_delivery_simulator.exception.OrderPayloadException;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.repository.InMemoryOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final InMemoryOrderRepository orderRepository;
    private final DroneService droneService;
    private final RouteService routeService;
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Order createOrder(OrderRequestDTO dto) {
        if (dto.payloadKg() <= 0) {
            throw new OrderPayloadException("Peso deve ser maior que zero.");
        }

        boolean isDeliverable = droneService.getAllDrones().stream()
            .anyMatch(drone -> {
                double distance = routeService.calculateDistance(
                    0, 0,
                    dto.clientPositionX(), dto.clientPositionY()
                );
                return drone.maxPayloadKg() >= dto.payloadKg()
                    && drone.maxRangeKm() >= distance * 2;
            });

        OrderState orderState = isDeliverable ? OrderState.PENDING : OrderState.RECUSED;

        Order order = new Order(
            idGenerator.getAndIncrement(),
            dto.clientPositionX(),
            dto.clientPositionY(),
            dto.payloadKg(),
            dto.priority(),
            orderState
        );

        Order savedOrder = this.orderRepository.save(order);

        return savedOrder;
    }

    public List<Order> findAll(){
        return this.orderRepository.findAll();
    }
}