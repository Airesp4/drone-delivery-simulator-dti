package com.dti.drone_delivery_simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DroneDeliverySimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DroneDeliverySimulatorApplication.class, args);
	}

}
