package com.allo.orderservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String productId;
    private String name;
    private int quantity;
    private double price; // used double for simplicity, can be BigDecimal for precision or other robust tecniques
}