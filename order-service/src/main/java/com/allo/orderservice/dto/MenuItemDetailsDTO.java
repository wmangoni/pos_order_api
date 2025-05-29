package com.allo.orderservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDetailsDTO {
    private String id;
    private String name;
    private String description;
    private double price;
    private LocalDateTime createdAt;
}