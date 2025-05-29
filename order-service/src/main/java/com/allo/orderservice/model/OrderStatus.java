package com.allo.orderservice.model;

public enum OrderStatus {
    CREATED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}