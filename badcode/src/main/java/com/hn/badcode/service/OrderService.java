package com.hn.badcode.service;

import com.hn.badcode.dto.OrderRequest;
import com.hn.badcode.dto.OrderResponse;
import com.hn.badcode.entities.OrderEntity;
import com.hn.badcode.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {


    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        if (request != null) {
            if (request.getCustomerId() != null && !request.getCustomerId().isBlank()) {

                // BUSINESS RULE:
                // An order must have a positive amount.
                if (request.getAmount() > 0) {

                    OrderEntity order = new OrderEntity();
                    order.setCustomerId(request.getCustomerId());
                    order.setTotalAmount(request.getAmount());

                    OrderEntity savedOrder = orderRepository.save(order);

                    Map<String, Object> paymentPayload = new HashMap<>();
                    paymentPayload.put("orderId", savedOrder.getId());
                    paymentPayload.put("amount", savedOrder.getTotalAmount());

                    // BUSINESS RULE:
                    // Orders above 1000 are considered high-value orders
                    // and require payment processing.
                    if (savedOrder.getTotalAmount() > 1000) {
                        savedOrder.setStatus("WAITING_PAYMENT");

                        String gatewayResponse = restTemplate.postForObject(
                                "https://api.paymentgateway.com/charges",
                                paymentPayload,
                                String.class
                        );

                        if (gatewayResponse != null) {

                            // BUSINESS RULE:
                            // A successful payment completes the order.
                            if (gatewayResponse.equalsIgnoreCase("SUCCESS")) {
                                savedOrder.setStatus("ORDER_COMPLETED");

                                // BUSINESS RULE:
                                // A failed payment marks the order as failed.
                            } else if (gatewayResponse.equalsIgnoreCase("FAILED")) {
                                savedOrder.setStatus("ORDER_FAILED");

                                // BUSINESS RULE:
                                // A payment processing error marks the order
                                // as having a processing error.
                            } else {
                                if (gatewayResponse.equalsIgnoreCase("ERROR")) {
                                    savedOrder.setStatus("ORDER_PROCESSING_ERROR");
                                }
                            }

                            List<OrderEntity> customerOrders = orderRepository.findByCustomerId(request.getCustomerId());

                            double previousHighValueOrdersTotal = 0;

                            for (OrderEntity previousOrder : customerOrders) {
                                if (previousOrder != null) {
                                    if (previousOrder.getStatus() != null) {
                                        if (previousOrder.getStatus().equals("ORDER_COMPLETED")) {

                                            // BUSINESS RULE:
                                            // Only completed high-value orders
                                            // contribute to the customer's
                                            // previous high-value order total.
                                            if (previousOrder.getTotalAmount() > 1000) {
                                                previousHighValueOrdersTotal = previousHighValueOrdersTotal + previousOrder.getTotalAmount();
                                            }
                                        }
                                    }
                                }
                            }

                            // BUSINESS RULE:
                            // A customer becomes a VIP when their previous
                            // completed high-value orders exceed 5000.
                            if (previousHighValueOrdersTotal > 5000) {
                                savedOrder.setCustomerType("VIP_CUSTOMER");
                            }

                            savedOrder = orderRepository.save(savedOrder);
                        }
                    }

                    OrderResponse orderResponse = new OrderResponse(savedOrder.getId(), savedOrder.getCustomerId(), savedOrder.getTotalAmount());

                    return orderResponse;

                } else {
                    throw new IllegalArgumentException("Amount must be greater than zero");
                }

            } else {
                throw new IllegalArgumentException("Customer ID is required");
            }
        }

        throw new IllegalArgumentException("Order request is required");
    }
}
