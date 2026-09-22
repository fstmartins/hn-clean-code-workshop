package com.hn.workshop.adapters.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@Getter
public class OrderResponse {
    private Long id;
    private String customerId;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;
}