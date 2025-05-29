package com.allo.orderservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    private Customer customer;
    private List<OrderItem> orderItems;
    private double totalAmount;
    private OrderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}