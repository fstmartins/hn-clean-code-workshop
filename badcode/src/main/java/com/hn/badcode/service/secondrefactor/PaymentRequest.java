package com.hn.badcode.service.secondrefactor;

public record PaymentRequest(
        Long orderId,
        Money amount
) {
}