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

    public void sendOrderStatusUpdate(String orderId, String status, String customerFullName, String customerAddress, String customerEmail) {

        Map<String, Object> messagePayload = new HashMap<>();
        messagePayload.put("orderId", orderId);
        messagePayload.put("newStatus", status);
        messagePayload.put("customerFullName", customerFullName);
        messagePayload.put("customerAddress", customerAddress);
        messagePayload.put("customerEmail", customerEmail);

        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, messagePayload);
            logger.info("Sent order status update message to RabbitMQ for order ID {}: {}", orderId, messagePayload);
        } catch (Exception e) {
            logger.error("Error sending message to RabbitMQ for order ID {}: {}", orderId, e.getMessage());
            // We can implement retry logic or dead-letter queue for production
        }
    }
}