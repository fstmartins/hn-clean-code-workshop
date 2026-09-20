package com.hn.workshop.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {

    @NotBlank
    private String customerId;

    @NotNull
    @Positive
    private BigDecimal totalAmount;
}