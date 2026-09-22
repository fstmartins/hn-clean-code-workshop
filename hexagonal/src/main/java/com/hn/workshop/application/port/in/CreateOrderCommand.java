package com.hn.workshop.application.port.in;

import java.math.BigDecimal;

public record CreateOrderCommand(
        String customerId,
        BigDecimal totalAmount
) {
}