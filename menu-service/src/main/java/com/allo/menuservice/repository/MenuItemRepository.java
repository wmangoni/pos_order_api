package com.allo.menuservice.repository;

import com.allo.menuservice.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends MongoRepository<MenuItem, String> {

    Page<MenuItem> findAll(Pageable pageable);
}