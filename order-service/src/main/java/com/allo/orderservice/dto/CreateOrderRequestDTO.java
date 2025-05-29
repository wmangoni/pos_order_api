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
    @Valid
    private CustomerDTO customer;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequestDTO> orderItems;
}