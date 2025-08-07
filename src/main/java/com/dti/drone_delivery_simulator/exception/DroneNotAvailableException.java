package com.dti.drone_delivery_simulator.exception;

public class DroneNotAvailableException extends RuntimeException {
    public DroneNotAvailableException(String message) {
        super(message);
    }
}
