package com.hn.workshop.application.port.out;

import com.hn.workshop.domain.enums.PaymentResult;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentResult process(Long orderId, BigDecimal amount);
}