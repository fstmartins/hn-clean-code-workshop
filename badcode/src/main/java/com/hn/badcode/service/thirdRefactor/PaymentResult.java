package com.hn.badcode.service.thirdRefactor;

public sealed interface PaymentResult
        permits PaymentSuccess, PaymentFailed, PaymentError {
}