package com.B2B.events.account;

import com.B2B.events.BaseEvent;
import com.B2B.extra.Currency;
import com.B2B.extra.RejectionCause;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentRejectedV1 extends BaseEvent
{
    private final UUID receiverAccountId;
    private final UUID senderAccountId;
    private final UUID paymentId;
    private final BigDecimal amount;
    private final Currency currency;
    private final Instant requestedAt;
    private final Instant rejectedAt;
    private final String paymentCause;
    private final RejectionCause rejectionCause;


    @JsonCreator
    public PaymentRejectedV1(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("receiverAccountId") UUID receiverAccountId,
            @JsonProperty("paymentId") UUID paymentId,
            @JsonProperty("senderAccountId") UUID senderAccountId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") Currency currency,
            @JsonProperty("requestedAt") Instant requestedAt,
            @JsonProperty("rejectedAt") Instant rejectedAt,
            @JsonProperty("paymentCause") String paymentCause,
            @JsonProperty("rejectionCause") RejectionCause rejectionCause)
    {
        super(eventId, Instant.now());
        this.receiverAccountId = receiverAccountId;
        this.paymentId = paymentId;
        this.senderAccountId = senderAccountId;
        this.amount = amount;
        this.currency = currency;
        this.requestedAt = requestedAt;
        this.rejectedAt = rejectedAt;
        this.paymentCause = paymentCause;
        this.rejectionCause = rejectionCause;
    }

    public UUID getReceiverAccountId()
    {
        return receiverAccountId;
    }

    public UUID getSenderAccountId()
    {
        return senderAccountId;
    }

    public UUID getPaymentId()
    {
        return paymentId;
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

    public Instant getRejectedAt()
    {
        return rejectedAt;
    }

    public String getPaymentCause()
    {
        return paymentCause;
    }

    public RejectionCause getRejectionCause()
    {
        return rejectionCause;
    }
}
