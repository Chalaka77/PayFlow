package com.B2B.events.payment;

import com.B2B.events.BaseEvent;
import com.B2B.extra.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentRequestedV1 extends BaseEvent
{
    private final UUID paymentId;
    private final UUID senderAccountId;
    private final UUID receiverAccountId;
    private final BigDecimal amount;
    private final Currency currency;
    private final Instant requestedAt;
    private final String paymentCause;

    public PaymentRequestedV1(UUID eventId, UUID paymentId, UUID senderAccountId, UUID receiverAccountId, BigDecimal amount, Currency currency, Instant requestedAt, String paymentCause)
    {
        super(eventId, Instant.now());
        this.paymentId = paymentId;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.currency = currency;
        this.requestedAt = requestedAt;
        this.paymentCause = paymentCause;
    }

    public UUID getPaymentId()
    {
        return paymentId;
    }

    public UUID getSenderAccountId()
    {
        return senderAccountId;
    }

    public UUID getReceiverAccountId()
    {
        return receiverAccountId;
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

    public String getPaymentCause()
    {
        return paymentCause;
    }

}
