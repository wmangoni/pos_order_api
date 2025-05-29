package com.allo.orderservice.service;

import com.allo.orderservice.dto.*;
import com.allo.orderservice.model.Customer;
import com.allo.orderservice.model.Order;
import com.allo.orderservice.model.OrderItem;
import com.allo.orderservice.model.OrderStatus; // Assuming OrderStatus enum is in model package
import com.allo.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final RabbitMQSender rabbitMQSender;

    @Value("${menu.service.baseurl}")
    private String menuServiceBaseUrl;

    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate, RabbitMQSender rabbitMQSender) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
        this.rabbitMQSender = rabbitMQSender;
    }

    @Transactional
    public OrderResponseDTO createOrder(CreateOrderRequestDTO requestDTO) {
        Order order = new Order();

        Customer customer = new Customer();
        BeanUtils.copyProperties(requestDTO.getCustomer(), customer);
        order.setCustomer(customer);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0;

        // Fetch item details from Menu Service and calculate total amount
        for (OrderItemRequestDTO itemRequest : requestDTO.getOrderItems()) {
            MenuItemDetailsDTO menuItemDetails = fetchMenuItemDetails(itemRequest.getProductId());
            if (menuItemDetails == null) {
                // Handle case where menu item is not found or menu service is down
                throw new MenuItemNotFoundException("Menu item not found with ID: " + itemRequest.getProductId());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(menuItemDetails.getId());
            orderItem.setName(menuItemDetails.getName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(menuItemDetails.getPrice());
            orderItems.add(orderItem);

            totalAmount += menuItemDetails.getPrice() * itemRequest.getQuantity();
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {}", savedOrder.getId());

        // Optionally, we can publish an event here
        // rabbitMQSender.sendOrderStatusUpdate(savedOrder.getId(), savedOrder.getStatus().name(), savedOrder.getCustomer());


        return convertToResponseDTO(savedOrder);
    }

    private MenuItemDetailsDTO fetchMenuItemDetails(String productId) {
        String url = menuServiceBaseUrl + "/" + productId;
        try {
            MenuItemDetailsDTO response = restTemplate.getForObject(url, MenuItemDetailsDTO.class);
            logger.debug("Fetched menu item details for productId {}: {}", productId, response);
            return response;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Menu item with ID {} not found in MenuService.", productId);
                return null;
            }
            logger.error("Error fetching menu item details for productId {}: {}", productId, e.getMessage());
            throw e; // Re-throw other client errors
        } catch (Exception e) {
            logger.error("Unexpected error fetching menu item details for productId {}: {}", productId, e.getMessage());
            // We can have a custom exception or fallback mechanism here
            throw new RuntimeException("Error communicating with Menu Service for product ID: " + productId, e);
        }
    }

    @Transactional
    public Optional<OrderResponseDTO> updateOrderStatus(String orderId, String newStatusStr) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            try {
                OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
                // We can be valid status transitions here if needed: Ex,: if (isValidTransition(order.getStatus(), newStatus))
                order.setStatus(newStatus);
                order.setUpdatedAt(LocalDateTime.now());
                Order updatedOrder = orderRepository.save(order);
                logger.info("Order status updated for order ID {}: {}", orderId, newStatus);

                rabbitMQSender.sendOrderStatusUpdate(
                    updatedOrder.getId(),
                    updatedOrder.getStatus().name(),
                    updatedOrder.getCustomer().getFullName(),
                    updatedOrder.getCustomer().getAddress(),
                    updatedOrder.getCustomer().getEmail()
                );

                return Optional.of(convertToResponseDTO(updatedOrder));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid status value provided: {}", newStatusStr);
                throw new InvalidOrderStatusException("Invalid status value: " + newStatusStr +
                ". Allowed values are: " + java.util.Arrays.toString(OrderStatus.values()));
            }
        }
        return Optional.empty(); // Order not found
    }

    public PaginatedOrderResponseDTO getOrderHistory(int limit, int offset) {
        if (limit <= 0) limit = 10;
        if (offset < 0) offset = 0;

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Order> orderPage = orderRepository.findAll(pageable);

        List<OrderResponseDTO> dtoList = orderPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return new PaginatedOrderResponseDTO(dtoList, limit, offset, orderPage.getTotalElements());
    }

    public Optional<OrderResponseDTO> getOrderById(String orderId) {
        return orderRepository.findById(orderId).map(this::convertToResponseDTO);
    }

    // Helper to convert Order Entity to OrderResponseDTO
    private OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        BeanUtils.copyProperties(order, dto, "customer", "orderItems", "status");

        CustomerDTO customerDTO = new CustomerDTO();
        if (order.getCustomer() != null) {
            BeanUtils.copyProperties(order.getCustomer(), customerDTO);
        }
        dto.setCustomer(customerDTO);

        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream().map(itemEntity -> {
                OrderItemResponseDTO itemDTO = new OrderItemResponseDTO();
                BeanUtils.copyProperties(itemEntity, itemDTO);
                return itemDTO;
            }).collect(Collectors.toList()));
        }

        if (order.getStatus() != null) {
            dto.setStatus(order.getStatus().name());
        }
        
        dto.setId(order.getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());


        return dto;
    }

    public static class MenuItemNotFoundException extends RuntimeException {
        public MenuItemNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidOrderStatusException extends RuntimeException {
        public InvalidOrderStatusException(String message) {
            super(message);
        }
    }
}
