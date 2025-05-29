package com.allo.menuservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing; // If using @CreatedDate/@LastModifiedDate

@SpringBootApplication
@EnableMongoAuditing // Enable this if you want Spring Data to manage createdAt/updatedAt automatically
public class MenuServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenuServiceApplication.class, args);
    }

}