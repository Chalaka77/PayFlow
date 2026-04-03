package com.B2B.events.account;

import com.B2B.events.BaseEvent;
import com.B2B.extra.Currency;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentAcceptedV1 extends BaseEvent
{
    private final UUID receiverAccountId;
    private final UUID senderAccountId;
    private final UUID paymentId;
    private final BigDecimal amount;
    private final Currency currency;
    private final Instant requestedAt;
    private final Instant acceptedAt;
    private final String paymentCause;

    @JsonCreator
    public PaymentAcceptedV1(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("receiverAccountId") UUID receiverAccountId,
            @JsonProperty("paymentId") UUID paymentId,
            @JsonProperty("senderAccountId") UUID senderAccountId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") Currency currency,
            @JsonProperty("requestedAt") Instant requestedAt,
            @JsonProperty("acceptedAt") Instant acceptedAt,
            @JsonProperty("paymentCause") String paymentCause)
    {
        super(eventId, Instant.now());
        this.receiverAccountId = receiverAccountId;
        this.paymentId = paymentId;
        this.senderAccountId = senderAccountId;
        this.amount = amount;
        this.currency = currency;
        this.requestedAt = requestedAt;
        this.acceptedAt = acceptedAt;
        this.paymentCause = paymentCause;
    }

    public UUID getReceiverAccountId()
    {
        return receiverAccountId;
    }

    public UUID getPaymentId()
    {
        return paymentId;
    }

    public UUID getSenderAccountId()
    {
        return senderAccountId;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public Currency getCurrency()
    {
        return currency;
    }

    public Instant getRequestedAt()
    {
        return requestedAt;
    }

    public Instant getAcceptedAt()
    {
        return acceptedAt;
    }

    public String getPaymentCause()
    {
        return paymentCause;
    }
}
