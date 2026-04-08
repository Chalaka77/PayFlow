package com.b2b.paymentservice.api.dto;

import com.B2B.extra.Currency;
import com.B2B.extra.StatusPayment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        StatusPayment status,
        Instant paymentRequestedAt,
        BigDecimal amount,
        Currency currency,
        String reasonPayment,
        String rejectionCause)
{
}
