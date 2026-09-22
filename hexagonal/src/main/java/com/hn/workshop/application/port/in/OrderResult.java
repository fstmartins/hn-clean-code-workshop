package com.hn.workshop.application.port.in;

import com.hn.workshop.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResult(
        Long id,
        String customerId,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt
) {
}