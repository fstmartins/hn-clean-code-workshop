package com.hn.workshop.adapters.out.payment;

import com.hn.workshop.application.port.out.PaymentGateway;
import com.hn.workshop.domain.enums.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PaymentGatewayAdapter implements PaymentGateway {

    private final RestTemplate restTemplate;

    private static final String URL =
            "https://api.paymentgateway.com/charges";

    @Override
    public PaymentResult process(Long id, BigDecimal amount) {

        try {
            PaymentResponse response = callPaymentApi(id, amount);
            return getPaymentResult(response);
        } catch (RestClientException e) {
            return PaymentResult.ERROR;
        }
    }

    private PaymentResponse callPaymentApi(Long id, BigDecimal amount) {
        return restTemplate.postForObject(
                URL,
                createPaymentRequest(id, amount),
                PaymentResponse.class
        );
    }

    private PaymentRequest createPaymentRequest(Long orderId, BigDecimal amount) {
        return new PaymentRequest(
                orderId,
                amount
        );
    }

    private PaymentResult getPaymentResult(PaymentResponse response) {
        if (response == null) {
            return PaymentResult.ERROR;
        }
        return "SUCCESS".equalsIgnoreCase(response.status())
                ? PaymentResult.SUCCESS
                : PaymentResult.FAILED;
    }
}
