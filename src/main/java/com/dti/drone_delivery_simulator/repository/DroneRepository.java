package com.dti.drone_delivery_simulator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.model.Drone;
import com.dti.drone_delivery_simulator.model.Order;

@Repository
public interface DroneRepository {
    List<Drone> findAll();
    Optional<Drone> findById(Long id);
    List<Drone> findByState(DroneState state);
    
    Drone addOrderToDrone(Long droneId, Order order);
    Drone removeOrderFromDrone(Long droneId, Long orderId);
    Drone advanceDroneState(Long droneId);
}
