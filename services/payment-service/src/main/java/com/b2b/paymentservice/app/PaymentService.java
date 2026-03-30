package com.b2b.paymentservice.app;

import com.b2b.paymentservice.api.dto.PaymentRequest;
import com.b2b.paymentservice.api.dto.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService
{
    PaymentResponse createPayment(PaymentRequest paymentRequest);
    PaymentResponse getPayment(UUID paymentId);
    List<PaymentResponse> getPayments();

}
