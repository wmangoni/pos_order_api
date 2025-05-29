package com.allo.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RabbitMQConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);

    // Consumes messages from the order.status.queue
    // The queue name is taken from application.properties via SpEL
    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receiveOrderStatusUpdate(Map<String, Object> messagePayload) {
        // The message payload will be a Map if Jackson2JsonMessageConverter is used
        // and the producer sends a Map or a compatible JSON object.
        logger.info("Received order status update from RabbitMQ: {}", messagePayload);

        // Simulate sending a notification
        String orderId = (String) messagePayload.get("orderId");
        String newStatus = (String) messagePayload.get("newStatus");
        String customerFullName = (String) messagePayload.get("customerFullName");
        String customerAddress = (String) messagePayload.get("customerAddress");
        String customerEmail = (String) messagePayload.get("customerEmail");

        // Log the notification details
        logger.info("--- SIMULATING CUSTOMER NOTIFICATION ---");
        logger.info("To: {} ({})", customerFullName, customerEmail);
        logger.info("Address: {}", customerAddress);
        logger.info("Your order #{} status has been updated to: {}", orderId, newStatus);
        logger.info("--------------------------------------");

        // In a real application, this would integrate with an email service, SMS service, etc.
    }
}
