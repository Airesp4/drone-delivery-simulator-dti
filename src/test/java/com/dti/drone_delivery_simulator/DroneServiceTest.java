package com.dti.drone_delivery_simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dti.drone_delivery_simulator.dto.DroneStatusResponseDTO;
import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.model.Drone;
import com.dti.drone_delivery_simulator.repository.InMemoryDroneRepository;
import com.dti.drone_delivery_simulator.service.DroneService;

@ExtendWith(MockitoExtension.class)
class DroneServiceTest {

    @Mock
    private InMemoryDroneRepository droneRepository;

    @InjectMocks
    private DroneService droneService;

    private List<Drone> mockDrones;

    @BeforeEach
    void setUp() {
        Drone drone1 = new Drone(1L, 10.0, 20.0, DroneState.IDLE, 0, 0, null);
        Drone drone2 = new Drone(2L, 15.0, 25.0, DroneState.DELIVERING, 0, 0, null);
        mockDrones = Arrays.asList(drone1, drone2);
    }

    @Test
    void testGetAllDrones_ShouldReturnListOfDroneStatusResponseDTO() {

        when(droneRepository.findAll()).thenReturn(mockDrones);

        List<DroneStatusResponseDTO> result = droneService.getAllDrones();

        assertEquals(mockDrones.size(), result.size());

        DroneStatusResponseDTO dto1 = result.get(0);
        assertEquals(1L, dto1.id());
        assertEquals(10.0, dto1.maxPayloadKg());
        assertEquals(20.0, dto1.maxRangeKm());
        assertEquals(DroneState.IDLE, dto1.status());

        DroneStatusResponseDTO dto2 = result.get(1);
        assertEquals(2L, dto2.id());
        assertEquals(15.0, dto2.maxPayloadKg());
        assertEquals(25.0, dto2.maxRangeKm());
        assertEquals(DroneState.DELIVERING, dto2.status());
    }
}
