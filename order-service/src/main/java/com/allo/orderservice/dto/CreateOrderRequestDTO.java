package com.allo.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDTO {
    @NotNull(message = "Customer information cannot be null")
    @Valid // Enable validation for nested CustomerDTO
    private CustomerDTO customer;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid // Enable validation for list of OrderItemRequestDTO
    private List<OrderItemRequestDTO> orderItems;
}