package com.hn.badcode.service.secondrefactor;

import java.math.BigDecimal;

public record Money(BigDecimal value) {

    public Money {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public boolean isGreaterThan(Money other) {
        return value.compareTo(other.value) > 0;
    }

    public Money add(Money other) {
        return new Money(value.add(other.value));
    }
}
