package com.hn.badcode.repositories;

import com.hn.badcode.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// BAD: DB persistence interface directly referenced by business services
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByCustomerId(String customerId);
}