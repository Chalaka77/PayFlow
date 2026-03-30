package com.b2b.paymentservice.api.dto;

import com.B2B.extra.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public record PaymentRequest(
        @NotNull UUID senderAccountId,
        @NotNull UUID receiverAccountId,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be greater than zero")BigDecimal amount,
        @NotNull Currency currency,
        Optional<String> reasonPayment)
{
}
