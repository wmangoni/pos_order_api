package com.allo.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RabbitMQSender {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQSender.class);
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;

    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Method to send order status update message
    public void sendOrderStatusUpdate(String orderId, String status, String customerFullName, String customerAddress, String customerEmail) {
        // Create a map or a dedicated DTO for the message payload
        Map<String, Object> messagePayload = new HashMap<>();
        messagePayload.put("orderId", orderId);
        messagePayload.put("newStatus", status);
        messagePayload.put("customerFullName", customerFullName);
        messagePayload.put("customerAddress", customerAddress);
        messagePayload.put("customerEmail", customerEmail);
        // Add any other relevant information for notification

        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, messagePayload);
            logger.info("Sent order status update message to RabbitMQ for order ID {}: {}", orderId, messagePayload);
        } catch (Exception e) {
            logger.error("Error sending message to RabbitMQ for order ID {}: {}", orderId, e.getMessage());
            // Implement retry logic or dead-letter queue for production
        }
    }
}