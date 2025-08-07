package com.dti.drone_delivery_simulator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dti.drone_delivery_simulator.enums.RouteStatus;
import com.dti.drone_delivery_simulator.model.Route;

@Repository
public interface RouteRepository {
    
    Route save(Route route);
    Optional<Route> findById(Long id);
    List<Route> findAll();
    Route updateStatus(Long id, RouteStatus status);
}
