package com.allo.orderservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private String id;
    private CustomerDTO customer;
    private List<OrderItemResponseDTO> orderItems;
    private double totalAmount;
    private String status; // Using String for status as per example "CREATED"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // Present on updates
}