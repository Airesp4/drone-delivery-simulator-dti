package com.dti.drone_delivery_simulator.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dti.drone_delivery_simulator.dto.RouteResponseDTO;
import com.dti.drone_delivery_simulator.service.RouteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/entregas")
@RequiredArgsConstructor
public class RouteController {
    
    private final RouteService routeService;

    @GetMapping("/rota")
    public ResponseEntity<List<RouteResponseDTO>> getAllRoutes() {
        return ResponseEntity.ok(routeService.findAllRoutes());
    }
}
