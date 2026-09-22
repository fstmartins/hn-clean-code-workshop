package com.hn.badcode.service.secondrefactor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class Order {

    private static final Money HIGH_VALUE_THRESHOLD = new Money(BigDecimal.valueOf(1000));

    private final Long id;
    private final CustomerId customerId;
    private final Money totalAmount;
    private OrderStatus status;
    private CustomerType customerType;


    public boolean isHighValue() {
        return totalAmount.isGreaterThan(HIGH_VALUE_THRESHOLD);
    }

    public void markAsVip() {
        this.customerType = CustomerType.VIP;
    }
}