package com.allo.orderservice.controller;

import com.allo.orderservice.dto.CreateOrderRequestDTO;
import com.allo.orderservice.dto.OrderResponseDTO;
import com.allo.orderservice.dto.OrderStatusUpdateDTO;
import com.allo.orderservice.dto.PaginatedOrderResponseDTO;
import com.allo.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequestDTO requestDTO) {
        try {
            OrderResponseDTO createdOrder = orderService.createOrder(requestDTO);
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (OrderService.MenuItemNotFoundException e) {
            logger.warn("Failed to create order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(Collections.singletonMap("error", e.getMessage()));
        } catch (HttpClientErrorException e) {
             logger.error("HTTP Client Error during order creation: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
             return ResponseEntity.status(e.getStatusCode())
                                 .body(Collections.singletonMap("error", "Error communicating with Menu Service: " + e.getResponseBodyAsString()));
        } catch (Exception e) {
            logger.error("Unexpected error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Collections.singletonMap("error", "An unexpected error occurred while creating the order."));
        }
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderId, @Valid @RequestBody OrderStatusUpdateDTO statusUpdateDTO) {
        try {
            return orderService.updateOrderStatus(orderId, statusUpdateDTO.getStatus().name())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (OrderService.InvalidOrderStatusException e) {
            logger.warn("Failed to update order status for order ID {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating order status for order ID {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Collections.singletonMap("error", "An unexpected error occurred while updating order status."));
        }
    }

    @GetMapping
    public ResponseEntity<PaginatedOrderResponseDTO> getOrderHistory(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PaginatedOrderResponseDTO response = orderService.getOrderHistory(limit, offset);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable String orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Global Exception Handler (Basic) - Can be moved to a @ControllerAdvice class
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, String> errors = new java.util.HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        Map<String, Object> responseBody = new java.util.HashMap<>();
        responseBody.put("message", "Validation failed");
        responseBody.put("errors", errors);
        logger.warn("Validation failed: {}", errors);
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }
}
