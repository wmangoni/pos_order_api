package com.allo.menuservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MenuItemResponseDTO {
    private String id;
    private String name;
    private String description;
    private double price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}