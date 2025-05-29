package com.allo.orderservice.service;

import com.allo.orderservice.dto.*;
import com.allo.orderservice.model.Customer;
import com.allo.orderservice.model.Order;
import com.allo.orderservice.model.OrderItem;
import com.allo.orderservice.model.OrderStatus;
import com.allo.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Initializes mocks
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitMQSender rabbitMQSender;

    @InjectMocks // Creates an instance of OrderService and injects mocks into it
    private OrderService orderService;

    private CreateOrderRequestDTO createOrderRequestDTO;
    private MenuItemDetailsDTO menuItemDetailsDTO;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        // Mock menu service base URL (normally from @Value)
        // This can be done via reflection or by making the field package-private and setting it
        // For simplicity, we'll assume it's correctly configured.
        // A better way for tests is to use @TestPropertySource or constructor injection for such values.
        // ReflectionTestUtils.setField(orderService, "menuServiceBaseUrl", "http://dummy-menu-service/menu-items");


        CustomerDTO customerDTO = new CustomerDTO("John Doe", "123 Main St", "john@example.com");
        OrderItemRequestDTO orderItemRequestDTO = new OrderItemRequestDTO("prod123", 2);
        createOrderRequestDTO = new CreateOrderRequestDTO(customerDTO, Collections.singletonList(orderItemRequestDTO));

        menuItemDetailsDTO = new MenuItemDetailsDTO("prod123", "Test Item", "Desc", 10.0, LocalDateTime.now());

        Customer customer = new Customer("John Doe", "123 Main St", "john@example.com");
        OrderItem orderItem = new OrderItem("prod123", "Test Item", 2, 10.0);
        savedOrder = new Order("orderId1", customer, Collections.singletonList(orderItem), 20.0, OrderStatus.CREATED, LocalDateTime.now(), null);
    }

    @Test
    void createOrder_success() {
        // Mock RestTemplate call to MenuService
        when(restTemplate.getForObject(anyString(), eq(MenuItemDetailsDTO.class))).thenReturn(menuItemDetailsDTO);
        // Mock repository save
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponseDTO result = orderService.createOrder(createOrderRequestDTO);

        assertNotNull(result);
        assertEquals("orderId1", result.getId());
        assertEquals("John Doe", result.getCustomer().getFullName());
        assertEquals(1, result.getOrderItems().size());
        assertEquals("Test Item", result.getOrderItems().get(0).getName());
        assertEquals(20.0, result.getTotalAmount());
        assertEquals("CREATED", result.getStatus());

        verify(restTemplate, times(1)).getForObject(anyString(), eq(MenuItemDetailsDTO.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        // verify(rabbitMQSender, times(1)).sendOrderStatusUpdate(anyString(), anyString(), any(Customer.class)); // If create publishes
    }

    @Test
    void createOrder_menuItemNotFound_throwsException() {
        when(restTemplate.getForObject(anyString(), eq(MenuItemDetailsDTO.class))).thenReturn(null);

        assertThrows(OrderService.MenuItemNotFoundException.class, () -> {
            orderService.createOrder(createOrderRequestDTO);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_success() {
        when(orderRepository.findById("orderId1")).thenReturn(Optional.of(savedOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved order
        doNothing().when(rabbitMQSender).sendOrderStatusUpdate(anyString(), anyString(), anyString(), anyString(), anyString());


        Optional<OrderResponseDTO> result = orderService.updateOrderStatus("orderId1", "PREPARING");

        assertTrue(result.isPresent());
        assertEquals("PREPARING", result.get().getStatus());
        assertNotNull(result.get().getUpdatedAt());

        verify(orderRepository, times(1)).findById("orderId1");
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(rabbitMQSender, times(1)).sendOrderStatusUpdate(
            eq("orderId1"), eq("PREPARING"), eq("John Doe"), eq("123 Main St"), eq("john@example.com")
        );
    }

    @Test
    void updateOrderStatus_orderNotFound() {
        when(orderRepository.findById("unknownId")).thenReturn(Optional.empty());

        Optional<OrderResponseDTO> result = orderService.updateOrderStatus("unknownId", "PREPARING");

        assertFalse(result.isPresent());
        verify(rabbitMQSender, never()).sendOrderStatusUpdate(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void updateOrderStatus_invalidStatus() {
        when(orderRepository.findById("orderId1")).thenReturn(Optional.of(savedOrder));

        assertThrows(OrderService.InvalidOrderStatusException.class, () -> {
             orderService.updateOrderStatus("orderId1", "INVALID_STATUS_VALUE");
        });
        verify(orderRepository, times(1)).findById("orderId1"); // Found
        verify(orderRepository, never()).save(any(Order.class)); // Not saved due to invalid status
        verify(rabbitMQSender, never()).sendOrderStatusUpdate(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void getOrderById_success() {
        when(orderRepository.findById("orderId1")).thenReturn(Optional.of(savedOrder));
        Optional<OrderResponseDTO> result = orderService.getOrderById("orderId1");
        assertTrue(result.isPresent());
        assertEquals("orderId1", result.get().getId());
    }
}
