package com.hn.workshop.domain.model;

import com.hn.workshop.domain.enums.OrderStatus;
import com.hn.workshop.domain.enums.PaymentResult;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
public class Order {

    public static final BigDecimal HIGH_VALUE_THRESHOLD = BigDecimal.valueOf(1000);
    private Long id;
    private String customerId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Instant createdAt;

    public static Order create(String customerId, BigDecimal totalAmount) {

        if (StringUtils.isBlank(customerId)) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        return new Order(
                null,
                customerId,
                totalAmount,
                OrderStatus.PENDING,
                Instant.now()
        );
    }

    public static Order reconstitute(Long id, String customerId, BigDecimal totalAmount, OrderStatus status, Instant createdAt) {
        return new Order(
                id,
                customerId,
                totalAmount,
                status,
                createdAt
        );
    }

    public void markHighValueProcessing() {
        this.status = OrderStatus.HIGH_VALUE_PROCESSING;
    }

    public void applyPaymentResult(PaymentResult result) {
        switch (result) {
            case SUCCESS -> this.status = OrderStatus.PAID;
            case FAILED -> this.status = OrderStatus.PAYMENT_FAILED;
            case ERROR -> this.status = OrderStatus.PAYMENT_ERROR;
        }
    }

    public boolean isHighValue() {
        return HIGH_VALUE_THRESHOLD.compareTo(this.totalAmount) < 0;
    }
}