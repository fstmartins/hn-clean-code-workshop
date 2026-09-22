package com.hn.badcode.service.thirdRefactor;

import com.hn.badcode.dto.OrderRequest;
import com.hn.badcode.dto.OrderResponse;
import com.hn.badcode.entities.OrderEntity;
import com.hn.badcode.repositories.OrderRepository;
import com.hn.badcode.service.secondrefactor.CustomerId;
import com.hn.badcode.service.secondrefactor.CustomerType;
import com.hn.badcode.service.secondrefactor.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderServiceThirdRefactor {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    private static final Money HIGH_VALUE_THRESHOLD = new Money(BigDecimal.valueOf(1000));
    private static final Money VIP_THRESHOLD = new Money(BigDecimal.valueOf(5000));


    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        validateRequest(request);

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

    private OrderEntity createOrderEntity(CustomerId customerId, Money amount) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(customerId.value());
        order.setTotalAmount(amount.value().doubleValue());

        return order;
    }

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

        PaymentResult paymentResult = parsePaymentResult(gatewayResponse);

        updateOrderStatus(order, paymentResult);

        Money previousHighValueOrdersTotal = calculatePreviousHighValueOrdersTotal(new CustomerId(order.getCustomerId()));

        if (previousHighValueOrdersTotal.isGreaterThan(VIP_THRESHOLD)) {
            order.setCustomerType(CustomerType.VIP.name());
        }

        return orderRepository.save(order);
    }

    private PaymentResult parsePaymentResult(String gatewayResponse) {

        if (gatewayResponse == null) {
            return new PaymentError();
        }

        return switch (gatewayResponse.toUpperCase()) {
            case "SUCCESS" -> new PaymentSuccess();
            case "FAILED" -> new PaymentFailed();
            default -> new PaymentError();
        };
    }

    // JAVA SEALED WITH SWITCH EXPRESSIONS:
    // The method now accepts a java sealed class
    // If a new paymentResult was added, the compiler would let you know this switch is not exhaustive
    // This prevents bugs down the line
    private void updateOrderStatus(OrderEntity order, PaymentResult paymentResult) {
        order.setStatus(switch (paymentResult) {
            case PaymentSuccess ignored -> "ORDER_COMPLETED";
            case PaymentFailed ignored -> "ORDER_FAILED";
            case PaymentError ignored -> "ORDER_PROCESSING_ERROR";
        });
    }

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

    private record PaymentRequest(
            Long orderId,
            Money amount
    ) {
    }
}

