package com.allo.orderservice.dto;

import com.allo.orderservice.enums.OrderStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateDTO {
    @NotNull(message = "Status cannot be null")
    private OrderStatus status;
}