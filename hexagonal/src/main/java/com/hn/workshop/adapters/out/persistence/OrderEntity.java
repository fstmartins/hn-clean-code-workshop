package com.hn.workshop.adapters.out.persistence;

import com.hn.workshop.domain.enums.OrderStatus;
import com.hn.workshop.domain.model.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Instant createdAt;


    public static OrderEntity fromDomain(Order order) {
        OrderEntity entity = new OrderEntity();

        entity.id = order.getId();
        entity.customerId = order.getCustomerId();
        entity.totalAmount = order.getTotalAmount();
        entity.status = order.getStatus();
        entity.createdAt = order.getCreatedAt();

        return entity;
    }

    public Order toDomain() {

        return Order.reconstitute(
                id,
                customerId,
                totalAmount,
                status,
                createdAt
        );
    }
}