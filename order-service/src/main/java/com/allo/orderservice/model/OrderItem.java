package com.allo.orderservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem { // Keep as package-private or make public if used elsewhere directly
    private String productId; // Corresponds to MenuItem ID
    private String name;      // Fetched from MenuService
    private int quantity;
    private double price;     // Fetched from MenuService (price per unit at time of order)
}