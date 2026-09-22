package com.hn.workshop.adapters.out.persistence;

import com.hn.workshop.application.port.out.OrderRepository;
import com.hn.workshop.domain.model.Order;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepository {

    private final SpringDataOrderRepository repository;

    @Override
    @Transactional
    public Order save(Order order) {

        OrderEntity entity = OrderEntity.fromDomain(order);

        OrderEntity saved = repository.save(entity);

        return saved.toDomain();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return repository
                .findById(id)
                .map(OrderEntity::toDomain);
    }
}