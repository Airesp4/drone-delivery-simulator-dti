package com.dti.drone_delivery_simulator;

import com.dti.drone_delivery_simulator.dto.RouteResponseDTO;
import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.enums.OrderPriority;
import com.dti.drone_delivery_simulator.enums.OrderState;
import com.dti.drone_delivery_simulator.enums.RouteStatus;
import com.dti.drone_delivery_simulator.exception.OrderNotFoundException;
import com.dti.drone_delivery_simulator.model.Drone;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.model.Route;
import com.dti.drone_delivery_simulator.repository.InMemoryRouteRepository;
import com.dti.drone_delivery_simulator.service.RouteService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private InMemoryRouteRepository routeRepository;

    @Spy
    @InjectMocks
    private RouteService routeService;

    private Drone drone;
    private Order order1;
    private Order order2;
    private Route route;

    @BeforeEach
    void setUp() {
        drone = new Drone(1L, 10.0, 100.0, DroneState.IDLE, 0, 0, new ArrayList<>());
        order1 = new Order(1L, 10, 0, 1.0, OrderPriority.LOW, OrderState.ALLOCATED);
        order2 = new Order(2L, 0, 10, 2.0, OrderPriority.MEDIUM, OrderState.ALLOCATED);
        route = new Route(1L, drone, Arrays.asList(order1, order2), 20.0, RouteStatus.PLANNED);
    }

    @Test
    void createRoute_WhenOrdersListIsEmpty_ThrowsOrderNotFoundException() {
        assertThrows(OrderNotFoundException.class, () -> routeService.createRoute(drone, Collections.emptyList()));
    }

    @Test
    void createRoute_WhenOrdersListIsNotEmpty_ReturnsCreatedRoute() {
        List<Order> orders = Arrays.asList(order1, order2);
        Route expectedRoute = new Route(1L, drone, orders, 34.14, RouteStatus.PLANNED);

        when(routeRepository.save(any(Route.class))).thenReturn(expectedRoute);
        doReturn(34.14).when(routeService).calculateRouteDistance(orders, 0, 0);

        Route createdRoute = routeService.createRoute(drone, orders);

        assertNotNull(createdRoute);
        assertEquals(expectedRoute.getDrone().getId(), createdRoute.getDrone().getId());
        assertEquals(expectedRoute.getTotalDistanceKm(), createdRoute.getTotalDistanceKm());
        assertEquals(expectedRoute.getOrders().size(), createdRoute.getOrders().size());
        verify(routeRepository, times(1)).save(any(Route.class));
    }

    @Test
    void updateStatusRoute_ShouldReturnRouteWithUpdatedStatus() {
        Route updatedRoute = new Route(1L, drone, Arrays.asList(order1), 20.0, RouteStatus.IN_PROGRESS);
        when(routeRepository.updateStatus(1L, RouteStatus.IN_PROGRESS)).thenReturn(updatedRoute);

        Route result = routeService.updateStatusRoute(1L, RouteStatus.IN_PROGRESS);

        assertNotNull(result);
        assertEquals(RouteStatus.IN_PROGRESS, result.getStatus());
        verify(routeRepository, times(1)).updateStatus(1L, RouteStatus.IN_PROGRESS);
    }

    @Test
    void findAllRoutes_ShouldReturnListOfRouteResponseDTOs() {
        when(routeRepository.findAll()).thenReturn(Arrays.asList(route));

        List<RouteResponseDTO> result = routeService.findAllRoutes();

        assertNotNull(result);
        assertEquals(1, result.size());
        RouteResponseDTO dto = result.get(0);
        assertEquals(route.getId(), dto.id());
        assertEquals(route.getStatus(), dto.status());
        assertEquals(route.getTotalDistanceKm(), dto.totalDistanceKm());
        assertEquals(route.getOrders().size(), dto.orders().size());
        assertEquals(route.getDrone().getId(), dto.drone().id());
    }

    @Test
    void calculateRouteDistance_WhenOrdersListIsEmpty_ReturnsZero() {
        double distance = routeService.calculateRouteDistance(Collections.emptyList(), 0, 0);

        assertEquals(0, distance);
    }

    @Test
    void calculateRouteDistance_WhenOrdersListIsNotEmpty_ReturnsCorrectDistance() {
        List<Order> orders = Arrays.asList(order1, order2);

        doReturn(10.0).when(routeService).calculateDistance(anyInt(), anyInt(), anyInt(), anyInt());

        double totalDistance = routeService.calculateRouteDistance(orders, 0, 0);

        assertEquals(30.0, totalDistance);
    }

    @Test
    void calculateDistance_ShouldReturnCorrectEuclideanDistance() {
        double distance = routeService.calculateDistance(0, 0, 3, 4);

        assertEquals(5.0, distance);
    }

    @Test
    void getOptimizedRoute_ShouldReturnOrdersSortedByClosestFirst() {
        Order order3 = new Order(3L, 5, 5, 3.0, OrderPriority.HIGH, OrderState.ALLOCATED);
        List<Order> orders = Arrays.asList(order1, order2, order3);

        doReturn(7.07).when(routeService).calculateDistance(0, 0, 5, 5);
        doReturn(10.0).when(routeService).calculateDistance(0, 0, 10, 0);
        doReturn(10.0).when(routeService).calculateDistance(0, 0, 0, 10);

        List<Order> optimized = routeService.getOptimizedRoute(orders);

        assertEquals(3, optimized.size());

        assertEquals(order3.getId(), optimized.get(0).getId());
    }
}