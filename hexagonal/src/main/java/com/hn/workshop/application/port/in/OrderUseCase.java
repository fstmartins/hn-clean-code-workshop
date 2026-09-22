package com.hn.workshop.application.port.in;

public interface OrderUseCase {

    OrderResult createOrder(CreateOrderCommand command);
}