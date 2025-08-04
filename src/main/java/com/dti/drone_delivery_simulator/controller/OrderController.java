package com.dti.drone_delivery_simulator.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dti.drone_delivery_simulator.dto.OrderRequestDTO;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;

    @PostMapping
    public Order createOrder(@RequestBody OrderRequestDTO request){
        return this.orderService.createOrder(request);
    }

    @GetMapping("/all")
    public List<Order> findAllOrders(){
        return this.orderService.findAll();
    }
}
