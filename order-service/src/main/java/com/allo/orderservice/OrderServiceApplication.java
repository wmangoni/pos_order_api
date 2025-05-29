package com.allo.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.data.mongodb.config.EnableMongoAuditing; // If using @CreatedDate/@LastModifiedDate

@SpringBootApplication
// @EnableMongoAuditing // If needed
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}