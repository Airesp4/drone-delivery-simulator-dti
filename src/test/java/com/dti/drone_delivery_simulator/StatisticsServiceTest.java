package com.dti.drone_delivery_simulator;

import com.dti.drone_delivery_simulator.dto.DroneStatusResponseDTO;
import com.dti.drone_delivery_simulator.dto.OrderSummaryDTO;
import com.dti.drone_delivery_simulator.dto.RouteResponseDTO;
import com.dti.drone_delivery_simulator.dto.StatisticsResponseDTO;
import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.enums.OrderPriority;
import com.dti.drone_delivery_simulator.enums.OrderState;
import com.dti.drone_delivery_simulator.enums.RouteStatus;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.service.DroneService;
import com.dti.drone_delivery_simulator.service.OrderService;
import com.dti.drone_delivery_simulator.service.RouteService;
import com.dti.drone_delivery_simulator.service.StatisticsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private RouteService routeService;

    @Mock
    private OrderService orderService;

    @Mock
    private DroneService droneService;

    @InjectMocks
    private StatisticsService statisticsService;

    private List<RouteResponseDTO> mockRoutes;
    private List<Order> mockOrders;
    private List<DroneStatusResponseDTO> mockDrones;

    @BeforeEach
    void setUp() {
        OrderSummaryDTO orderSummary1 = new OrderSummaryDTO(1L, 10, 10, 1.0, OrderPriority.LOW);
        OrderSummaryDTO orderSummary2 = new OrderSummaryDTO(2L, 20, 20, 2.0, OrderPriority.MEDIUM);
        OrderSummaryDTO orderSummary3 = new OrderSummaryDTO(3L, 30, 30, 3.0, OrderPriority.HIGH);

        RouteResponseDTO route1 = new RouteResponseDTO(1L, null, 10.0, RouteStatus.COMPLETED, Arrays.asList(orderSummary1));
        RouteResponseDTO route2 = new RouteResponseDTO(2L, null, 20.0, RouteStatus.COMPLETED, Arrays.asList(orderSummary2, orderSummary3));
        mockRoutes = Arrays.asList(route1, route2);

        Order order1 = new Order(1L, 10, 10, 1.0, OrderPriority.LOW, OrderState.DELIVERED);
        Order order2 = new Order(2L, 20, 20, 2.0, OrderPriority.MEDIUM, OrderState.DELIVERED);
        Order order3 = new Order(3L, 30, 30, 3.0, OrderPriority.HIGH, OrderState.PENDING);
        mockOrders = Arrays.asList(order1, order2, order3);

        DroneStatusResponseDTO drone1 = new DroneStatusResponseDTO(1L, 10.0, 100.0, DroneState.IDLE);
        DroneStatusResponseDTO drone2 = new DroneStatusResponseDTO(2L, 15.0, 150.0, DroneState.IDLE);
        DroneStatusResponseDTO drone3 = new DroneStatusResponseDTO(3L, 8.0, 80.0, DroneState.DELIVERING);
        mockDrones = Arrays.asList(drone1, drone2, drone3);
    }

    @Test
    void statisticsCalculation_ShouldReturnCorrectStatistics() {
        when(routeService.findAllRoutes()).thenReturn(mockRoutes);
        when(orderService.findAll()).thenReturn(mockOrders);
        when(droneService.getAllDrones()).thenReturn(mockDrones);

        StatisticsResponseDTO result = statisticsService.statisticsCalculation();

        assertEquals(2, result.totalRoutes());
        assertEquals(1.5, result.averageOrdersPerRoute());
        assertEquals(2, result.totalOrdersCompleted());
        assertEquals(2, result.dronesAvailable());
    }

    @Test
    void statisticsCalculation_WhenNoData_ShouldReturnZeroes() {
        when(routeService.findAllRoutes()).thenReturn(Collections.emptyList());
        when(orderService.findAll()).thenReturn(Collections.emptyList());
        when(droneService.getAllDrones()).thenReturn(Collections.emptyList());

        StatisticsResponseDTO result = statisticsService.statisticsCalculation();

        assertEquals(0, result.totalRoutes());
        assertEquals(0.0, result.averageOrdersPerRoute());
        assertEquals(0, result.totalOrdersCompleted());
        assertEquals(0, result.dronesAvailable());
    }
}