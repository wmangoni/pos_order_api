package com.allo.menuservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "menuItems")
public class MenuItem {

    @Id
    private String id;
    private String name;
    private String description;
    private double price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}