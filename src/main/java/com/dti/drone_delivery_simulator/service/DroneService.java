package com.dti.drone_delivery_simulator.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dti.drone_delivery_simulator.dto.DroneStatusResponseDTO;
import com.dti.drone_delivery_simulator.repository.InMemoryDroneRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DroneService {
    
    private final InMemoryDroneRepository droneRepository;

    public List<DroneStatusResponseDTO> getAllDrones() {
        return this.droneRepository.findAll().stream()
            .map(drone -> new DroneStatusResponseDTO(
                drone.getId(),
                drone.getMaxPayloadKg(),
                drone.getMaxRangeKm(),
                drone.getStatus()
            ))
            .collect(Collectors.toList());
    }
}
