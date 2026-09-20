package com.hn.workshop.adapters.in.web;

import com.hn.workshop.adapters.in.web.dto.OrderRequest;
import com.hn.workshop.adapters.in.web.dto.OrderResponse;
import com.hn.workshop.application.port.in.CreateOrderCommand;
import com.hn.workshop.application.port.in.OrderResult;
import com.hn.workshop.application.port.in.OrderUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderUseCase orderUseCase;

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {

        CreateOrderCommand command = new CreateOrderCommand(
                request.getCustomerId(),
                request.getTotalAmount()
        );

        OrderResult result = orderUseCase.createOrder(command);

        return new OrderResponse(
                result.id(),
                result.customerId(),
                result.totalAmount(),
                result.status().name(),
                result.createdAt()
        );
    }
}
