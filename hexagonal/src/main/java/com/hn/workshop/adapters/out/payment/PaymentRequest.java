package com.hn.workshop.adapters.out.payment;

import java.math.BigDecimal;

public record PaymentRequest(
        Long orderId,
        BigDecimal amount
) {}