package com.dti.drone_delivery_simulator.enums;

public enum DroneState {
    IDLE,
    LOADING,
    IN_FLIGHT,
    DELIVERING,
    RETURNING;

    public DroneState next() {
        return switch (this) {
            case IDLE -> LOADING;
            case LOADING -> IN_FLIGHT;
            case IN_FLIGHT -> DELIVERING;
            case DELIVERING -> RETURNING;
            case RETURNING -> IDLE;
        };
    }
}
