package com.allo.orderservice.repository;

import com.allo.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    // Provides CRUD operations

    // For paginated results
    Page<Order> findAll(Pageable pageable);
}