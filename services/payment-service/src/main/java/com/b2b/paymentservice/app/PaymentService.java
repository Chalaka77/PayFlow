package com.b2b.paymentservice.app;

import com.B2B.events.account.PaymentAcceptedV1;
import com.B2B.events.account.PaymentRejectedV1;
import com.B2B.extra.StatusPayment;
import com.b2b.paymentservice.api.dto.PaymentRequest;
import com.b2b.paymentservice.api.dto.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService
{
    PaymentResponse createPayment(PaymentRequest paymentRequest);
    PaymentResponse getPayment(UUID paymentId);
    List<PaymentResponse> getPayments();
    void updatePayment(PaymentAcceptedV1 event);
    void updatePayment(PaymentRejectedV1 event);
    void updatePayment(UUID paymentId, StatusPayment status);
}
