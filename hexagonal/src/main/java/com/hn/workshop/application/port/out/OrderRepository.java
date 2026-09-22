package com.hn.workshop.application.port.out;

import com.hn.workshop.domain.model.Order;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);
}