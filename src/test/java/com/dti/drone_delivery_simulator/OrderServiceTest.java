package com.dti.drone_delivery_simulator;

import com.dti.drone_delivery_simulator.dto.DroneStatusResponseDTO;
import com.dti.drone_delivery_simulator.dto.OrderRequestDTO;
import com.dti.drone_delivery_simulator.enums.DroneState;
import com.dti.drone_delivery_simulator.enums.OrderPriority;
import com.dti.drone_delivery_simulator.enums.OrderState;
import com.dti.drone_delivery_simulator.exception.OrderPayloadException;
import com.dti.drone_delivery_simulator.model.Order;
import com.dti.drone_delivery_simulator.repository.InMemoryOrderRepository;
import com.dti.drone_delivery_simulator.service.DroneService;
import com.dti.drone_delivery_simulator.service.OrderService;
import com.dti.drone_delivery_simulator.service.RouteService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private InMemoryOrderRepository orderRepository;

    @Mock
    private DroneService droneService;

    @Mock
    private RouteService routeService;

    @InjectMocks
    private OrderService orderService;

    @Spy
    private AtomicLong idGenerator = new AtomicLong(1);

    private OrderRequestDTO validOrderRequest;
    private DroneStatusResponseDTO capableDrone;
    private DroneStatusResponseDTO incapableDrone;

    @BeforeEach
    void setUp() {
        validOrderRequest = new OrderRequestDTO(10, 10, 5.0, OrderPriority.LOW);

        capableDrone = new DroneStatusResponseDTO(1L, 10.0, 30.0, DroneState.IDLE);
        incapableDrone = new DroneStatusResponseDTO(2L, 4.0, 10.0, DroneState.IDLE);
    }

    @Test
    void createOrder_WhenPayloadIsZero_ThrowsException() {
        OrderRequestDTO invalidDto = new OrderRequestDTO(10, 10, 0, OrderPriority.LOW);
        
        assertThrows(OrderPayloadException.class, () -> orderService.createOrder(invalidDto));
    }

    @Test
    void createOrder_WhenPayloadIsNegative_ThrowsException() {
        OrderRequestDTO invalidDto = new OrderRequestDTO(10, 10, -5.0, OrderPriority.LOW);

        assertThrows(OrderPayloadException.class, () -> orderService.createOrder(invalidDto));
    }

    @Test
    void createOrder_WhenThereIsADroneCapable_CreatesPendingOrder() {
        when(droneService.getAllDrones()).thenReturn(Arrays.asList(incapableDrone, capableDrone));
        when(routeService.calculateDistance(any(Integer.class), any(Integer.class), any(Integer.class), any(Integer.class))).thenReturn(10.0);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order createdOrder = orderService.createOrder(validOrderRequest);

        assertNotNull(createdOrder);
        assertEquals(OrderState.PENDING, createdOrder.getState());
        assertEquals(validOrderRequest.payloadKg(), createdOrder.getPayloadKg());
        assertEquals(1L, createdOrder.getId());
    }

    @Test
    void createOrder_WhenNoDroneIsCapable_CreatesRefusedOrder() {
        when(droneService.getAllDrones()).thenReturn(Arrays.asList(incapableDrone));
        when(routeService.calculateDistance(any(Integer.class), any(Integer.class), any(Integer.class), any(Integer.class))).thenReturn(10.0);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order createdOrder = orderService.createOrder(validOrderRequest);

        assertNotNull(createdOrder);
        assertEquals(OrderState.RECUSED, createdOrder.getState());
        assertEquals(validOrderRequest.payloadKg(), createdOrder.getPayloadKg());
    }

    @Test
    void createOrder_WhenNoDronesExist_CreatesRefusedOrder() {
        when(droneService.getAllDrones()).thenReturn(Collections.emptyList());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order createdOrder = orderService.createOrder(validOrderRequest);

        assertNotNull(createdOrder);
        assertEquals(OrderState.RECUSED, createdOrder.getState());
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        List<Order> orders = Arrays.asList(
            new Order(1L, 10, 10, 5.0, OrderPriority.LOW, OrderState.PENDING),
            new Order(2L, 20, 20, 8.0, OrderPriority.MEDIUM, OrderState.RECUSED)
        );
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.findAll();

        assertEquals(2, result.size());
        assertEquals(orders.get(0).getId(), result.get(0).getId());
        assertEquals(orders.get(1).getId(), result.get(1).getId());
    }
}