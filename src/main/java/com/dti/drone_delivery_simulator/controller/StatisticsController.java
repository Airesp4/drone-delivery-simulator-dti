package com.dti.drone_delivery_simulator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dti.drone_delivery_simulator.dto.StatisticsResponseDTO;
import com.dti.drone_delivery_simulator.service.StatisticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/estatisticas")
    public StatisticsResponseDTO getStatistics() {
        return statisticsService.statisticsCalculation();
    }
}
