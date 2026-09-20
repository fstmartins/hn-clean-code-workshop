package com.hn.badcode.dto;

public class OrderResponse {
    private Long orderId;
    private String status;
    private double amount;

    public OrderResponse(Long orderId, String status, double amount) {
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public double getAmount() {
        return amount;
    }
}