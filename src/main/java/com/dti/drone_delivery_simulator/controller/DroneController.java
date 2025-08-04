package com.dti.drone_delivery_simulator.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dti.drone_delivery_simulator.dto.DroneStatusResponseDTO;
import com.dti.drone_delivery_simulator.service.DroneService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/drones")
@RequiredArgsConstructor
public class DroneController {
    
    private final DroneService droneService;

    @GetMapping("/status")
    public List<DroneStatusResponseDTO> getDroneStatus() {
        return droneService.getAllDrones();
    }
}
