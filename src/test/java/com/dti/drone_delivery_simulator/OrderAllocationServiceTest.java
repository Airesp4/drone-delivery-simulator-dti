package com.dti.drone_delivery_simulator;

import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.enums.OrderPriority;
import com.dti.drone_delivery_simulator.enums.OrderState;
import com.dti.drone_delivery_simulator.model.Drone;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.repository.InMemoryDroneRepository;
import com.dti.drone_delivery_simulator.repository.InMemoryOrderRepository;
import com.dti.drone_delivery_simulator.service.OrderAllocationService;
import com.dti.drone_delivery_simulator.service.RouteService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderAllocationServiceTest {

    @Mock
    private InMemoryOrderRepository orderRepository;

    @Mock
    private InMemoryDroneRepository droneRepository;

    @Mock
    private RouteService routeService;

    @InjectMocks
    private OrderAllocationService orderAllocationService;

    private Drone drone;
    private Order compatibleOrder;
    private Order heavyOrder;
    private Order distantOrder;

    @BeforeEach
    void setUp() {
        drone = new Drone(1L, 20.0, 100.0, DroneState.IDLE, 0, 0, new ArrayList<>());

        compatibleOrder = new Order(1L, 10, 20, 5.0, OrderPriority.MEDIUM, OrderState.PENDING);
        heavyOrder = new Order(2L, 30, 40, 25.0, OrderPriority.HIGH, OrderState.PENDING);
        distantOrder = new Order(3L, 50, 50, 5.0, OrderPriority.LOW, OrderState.PENDING);
    }

    @Test
    void testAllocatePendingOrders_WhenDroneAndOrderAreCompatible() {
        List<Order> pendingOrders = new ArrayList<>(Arrays.asList(compatibleOrder));
        
        when(orderRepository.findPendingOrders()).thenReturn(pendingOrders);
        when(droneRepository.findByState(DroneState.IDLE)).thenReturn(Arrays.asList(drone));

        when(routeService.calculateRouteDistance(
                argThat(list -> list.contains(compatibleOrder)),
                anyInt(),
                anyInt()))
            .thenReturn(50.0);

        when(droneRepository.addOrderToDrone(drone.getId(), compatibleOrder)).thenReturn(drone);

        List<Drone> result = orderAllocationService.allocatePendingOrders();

        assertEquals(1, result.size());
        assertEquals(drone.getId(), result.get(0).getId());

        verify(droneRepository, times(1)).addOrderToDrone(drone.getId(), compatibleOrder);

        assertEquals(OrderState.ALLOCATED, compatibleOrder.getState());
        verify(orderRepository, times(1)).update(compatibleOrder);
    }

    @Test
    void testAllocatePendingOrders_WhenOrderIsTooHeavy() {
        when(orderRepository.findPendingOrders()).thenReturn(Arrays.asList(heavyOrder));
        when(droneRepository.findByState(DroneState.IDLE)).thenReturn(Arrays.asList(drone));

        List<Drone> result = orderAllocationService.allocatePendingOrders();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getOrders().isEmpty());

        verify(droneRepository, never()).addOrderToDrone(anyLong(), any(Order.class));
        assertEquals(OrderState.PENDING, heavyOrder.getState());
        verify(orderRepository, never()).update(any(Order.class));
    }

    @Test
    void testAllocatePendingOrders_WhenOrderIsTooDistant() {
        when(orderRepository.findPendingOrders()).thenReturn(Arrays.asList(distantOrder));
        when(droneRepository.findByState(DroneState.IDLE)).thenReturn(Arrays.asList(drone));

        when(routeService.calculateRouteDistance(
                argThat(list -> list.contains(distantOrder)),
                anyInt(),
                anyInt()))
            .thenReturn(150.0);

        List<Drone> result = orderAllocationService.allocatePendingOrders();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getOrders().isEmpty());

        verify(droneRepository, never()).addOrderToDrone(anyLong(), any(Order.class));
        assertEquals(OrderState.PENDING, distantOrder.getState());
        verify(orderRepository, never()).update(any(Order.class));
    }

    @Test
    void testAllocatePendingOrders_WhenNoPendingOrders() {
        when(orderRepository.findPendingOrders()).thenReturn(Collections.emptyList());
        when(droneRepository.findByState(DroneState.IDLE)).thenReturn(Arrays.asList(drone));

        List<Drone> result = orderAllocationService.allocatePendingOrders();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getOrders().isEmpty());
        verify(droneRepository, never()).addOrderToDrone(anyLong(), any(Order.class));
        verify(orderRepository, never()).update(any(Order.class));
    }

    @Test
    void testAllocatePendingOrders_WhenNoAvailableDrones() {
        when(orderRepository.findPendingOrders()).thenReturn(Arrays.asList(compatibleOrder));
        when(droneRepository.findByState(DroneState.IDLE)).thenReturn(Collections.emptyList());

        List<Drone> result = orderAllocationService.allocatePendingOrders();

        assertTrue(result.isEmpty());
        verify(droneRepository, never()).addOrderToDrone(anyLong(), any(Order.class));
        verify(orderRepository, never()).update(any(Order.class));
    }
}