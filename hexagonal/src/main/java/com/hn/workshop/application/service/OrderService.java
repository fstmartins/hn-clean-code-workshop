package com.hn.workshop.application.service;

import com.hn.workshop.application.port.in.CreateOrderCommand;
import com.hn.workshop.application.port.in.OrderResult;
import com.hn.workshop.application.port.in.OrderUseCase;
import com.hn.workshop.application.port.out.OrderRepository;
import com.hn.workshop.application.port.out.PaymentGateway;
import com.hn.workshop.domain.enums.PaymentResult;
import com.hn.workshop.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderUseCase {

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    @Override
    public OrderResult createOrder(CreateOrderCommand command) {

        Order order = Order.create(
                command.customerId(),
                command.totalAmount()
        );

        Order savedOrder = orderRepository.save(order);

        if (savedOrder.isHighValue()) {
            processPayment(savedOrder);
            savedOrder = orderRepository.save(savedOrder);
        }

        return toResult(savedOrder);
    }

    private void processPayment(Order order) {
        order.markHighValueProcessing();
        PaymentResult paymentResult = paymentGateway.process(order.getId(), order.getTotalAmount());
        order.applyPaymentResult(paymentResult);
    }


    private OrderResult toResult(Order order) {
        return new OrderResult(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }


}