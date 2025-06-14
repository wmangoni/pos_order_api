package com.allo.menuservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@TestConfiguration
public class TestMongoConfig {

    @Bean
    public MongoMappingContext mongoMappingContext() {
        return new MongoMappingContext();
    }
}