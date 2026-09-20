package com.hn.badcode.service.secondrefactor;

import com.hn.badcode.dto.OrderRequest;
import com.hn.badcode.dto.OrderResponse;
import com.hn.badcode.entities.OrderEntity;
import com.hn.badcode.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class OrderServiceSecondRefactor {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;


    private static final Money HIGH_VALUE_THRESHOLD = new Money(BigDecimal.valueOf(1000));
    private static final Money VIP_THRESHOLD = new Money(BigDecimal.valueOf(5000));


    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        validateRequest(request);

        // DOMAIN INTENT:
        // CustomerId and Money represent concepts from the domain
        // instead of passing primitive values throughout the application.
        CustomerId customerId = new CustomerId(request.getCustomerId());
        Money amount = new Money(BigDecimal.valueOf(request.getAmount()));

        OrderEntity order = createOrderEntity(customerId, amount);
        OrderEntity savedOrder = orderRepository.save(order);

        if (isHighValue(savedOrder)) {
            savedOrder = processHighValueOrder(savedOrder);
        }

        return createResponse(savedOrder);
    }


    private void validateRequest(OrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }
    }

    // DOMAIN INTENT:
    // The method now receives meaningful domain concepts rather
    // than a String and a primitive numeric value.
    private OrderEntity createOrderEntity(CustomerId customerId, Money amount) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(customerId.value());
        order.setTotalAmount(amount.value().doubleValue());

        return order;
    }

    // DOMAIN INTENT:
    // Money encapsulates the comparison instead of the service
    // comparing raw numeric values directly.
    private boolean isHighValue(OrderEntity order) {
        Money amount = new Money(BigDecimal.valueOf(order.getTotalAmount()));

        return amount.isGreaterThan(HIGH_VALUE_THRESHOLD);
    }

    private OrderEntity processHighValueOrder(OrderEntity order) {

        order.setStatus("WAITING_PAYMENT");

        PaymentRequest paymentRequest = new PaymentRequest(order.getId(), new Money(BigDecimal.valueOf(order.getTotalAmount())));

        String gatewayResponse = restTemplate.postForObject(
                "https://api.paymentgateway.com/charges",
                paymentRequest,
                String.class
        );

        if (gatewayResponse == null) {
            return order;
        }

        updateOrderStatus(order, gatewayResponse);

        Money previousHighValueOrdersTotal = calculatePreviousHighValueOrdersTotal(new CustomerId(order.getCustomerId()));

        // DOMAIN INTENT:
        // The service works with Money rather than comparing
        // a primitive numeric value against a magic number.
        if (previousHighValueOrdersTotal.isGreaterThan(VIP_THRESHOLD)) {
            order.setCustomerType(CustomerType.VIP.name());
        }

        return orderRepository.save(order);
    }

    private void updateOrderStatus(OrderEntity order, String gatewayResponse) {

        if (gatewayResponse.equalsIgnoreCase("SUCCESS")) {
            order.setStatus("ORDER_COMPLETED");
        } else if (gatewayResponse.equalsIgnoreCase("FAILED")) {
            order.setStatus("ORDER_FAILED");
        } else if (gatewayResponse.equalsIgnoreCase("ERROR")) {
            order.setStatus("ORDER_PROCESSING_ERROR");
        }
    }

    // DOMAIN INTENT:
    // The method accepts a CustomerId rather than a raw String,
    // making the type of identifier explicit.
    private Money calculatePreviousHighValueOrdersTotal(CustomerId customerId) {

        List<OrderEntity> customerOrders = orderRepository.findByCustomerId(customerId.value());

        return customerOrders.stream()
                .filter(Objects::nonNull)
                .filter(order -> "ORDER_COMPLETED".equals(order.getStatus()))
                .map(OrderEntity::getTotalAmount)
                .map(BigDecimal::valueOf)
                .map(Money::new)
                .filter(amount -> amount.isGreaterThan(HIGH_VALUE_THRESHOLD))
                .reduce(new Money(BigDecimal.ZERO), Money::add);
    }

    private OrderResponse createResponse(OrderEntity order) {
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getTotalAmount());
    }

}
