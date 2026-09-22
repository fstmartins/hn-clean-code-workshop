package com.hn.badcode.service.firstrefactor;

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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderServiceFirstRefactor {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // EARLY RETURNS:
        // Invalid cases are handled first so the main flow
        // doesn't become deeply nested.
        validateRequest(request);

        // STEP DOWN RULE:
        // At this level, we only care about WHAT happens,
        // not HOW each operation is implemented.
        OrderEntity order = createOrderEntity(request);
        OrderEntity savedOrder = orderRepository.save(order);

        if (isHighValue(savedOrder)) {
            savedOrder = processHighValueOrder(savedOrder);
        }

        return createResponse(savedOrder);
    }

    // STEP DOWN RULE:
    // Validation details are one level below createOrder().
    private void validateRequest(OrderRequest request) {

        // EARLY RETURN:
        // Deal with the exceptional case immediately.
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }

        // EARLY RETURN:
        // Don't allow invalid customer data to enter the main flow.
        if (request.getCustomerId() == null || request.getCustomerId().isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        // EARLY RETURN:
        // Don't allow invalid amounts to enter the main flow.
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    // STEP DOWN RULE:
    // The details of constructing the entity are hidden
    // from the main business flow.
    private OrderEntity createOrderEntity(OrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(request.getCustomerId());
        order.setTotalAmount(request.getAmount());

        return order;
    }

    // STEP DOWN RULE:
    // The decision is named instead of being embedded
    // inside the main workflow.
    private boolean isHighValue(OrderEntity order) {
        return order.getTotalAmount() > 1000;
    }

    // STEP DOWN RULE:
    // createOrder() doesn't need to know the details
    // of high-value order processing.
    private OrderEntity processHighValueOrder(OrderEntity order) {

        order.setStatus("WAITING_PAYMENT");

        Map<String, Object> paymentPayload = new HashMap<>();
        paymentPayload.put("orderId", order.getId());
        paymentPayload.put("amount", order.getTotalAmount());

        String gatewayResponse = restTemplate.postForObject(
                "https://api.paymentgateway.com/charges",
                paymentPayload,
                String.class
        );

        // EARLY RETURN:
        // If there is no gateway response, there is nothing
        // further to process.
        if (gatewayResponse == null) {
            return order;
        }

        updateOrderStatus(order, gatewayResponse);

        double previousHighValueOrdersTotal = calculatePreviousHighValueOrdersTotal(order.getCustomerId());

        if (previousHighValueOrdersTotal > 5000) {
            order.setCustomerType("VIP_CUSTOMER");
        }

        return orderRepository.save(order);
    }

    // STEP DOWN RULE:
    // Payment status mapping is hidden behind a method
    // that describes the operation at a higher level.
    private void updateOrderStatus(OrderEntity order, String gatewayResponse) {
        if (gatewayResponse.equalsIgnoreCase("SUCCESS")) {
            order.setStatus("ORDER_COMPLETED");
        } else if (gatewayResponse.equalsIgnoreCase("FAILED")) {
            order.setStatus("ORDER_FAILED");
        } else if (gatewayResponse.equalsIgnoreCase("ERROR")) {
            order.setStatus("ORDER_PROCESSING_ERROR");
        }
    }

    // STEP DOWN RULE:
    // The caller only needs to know that we're calculating
    // the previous high-value order total.
    private double calculatePreviousHighValueOrdersTotal(String customerId) {
        List<OrderEntity> customerOrders = orderRepository.findByCustomerId(customerId);

        // READABLE STREAM:
        // The pipeline expresses the business operation:
        //
        // customer orders
        //     -> completed orders
        //     -> high-value orders
        //     -> their amounts
        //     -> sum
        return customerOrders.stream()
                .filter(Objects::nonNull)
                .filter(order -> "ORDER_COMPLETED".equals(order.getStatus()))
                .filter(order -> order.getTotalAmount() > 1000)
                .mapToDouble(OrderEntity::getTotalAmount)
                .sum();
    }

    // STEP DOWN RULE:
    // Response construction is an implementation detail,
    // so the main workflow doesn't need to contain it.
    private OrderResponse createResponse(OrderEntity order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount()
        );
    }
}