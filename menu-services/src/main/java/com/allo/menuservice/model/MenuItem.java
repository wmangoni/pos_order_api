package com.allo.menuservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates a constructor with all arguments
@Document(collection = "menuItems") // Specifies the MongoDB collection name
public class MenuItem {

    @Id
    private String id; // MongoDB document ID

    private String name;
    private String description;
    private double price;
    // As per requirements, availability is not explicitly in the create/update payload,
    // but could be an implicit field or managed differently.
    // For now, keeping it simple as per the provided payloads.
    // private boolean available = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}