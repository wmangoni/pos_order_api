package com.allo.menuservice.repository;

import com.allo.menuservice.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends MongoRepository<MenuItem, String> {
    // MongoRepository provides CRUD operations (save, findById, findAll, deleteById, etc.)
    // Spring Data MongoDB will automatically implement these methods.

    // For paginated results, Spring Data Pageable can be used directly.
    // The controller will construct Pageable from limit and offset.
    Page<MenuItem> findAll(Pageable pageable);
}