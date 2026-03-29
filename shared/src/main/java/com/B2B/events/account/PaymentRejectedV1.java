package com.B2B.events.account;

import com.B2B.events.BaseEvent;
import com.B2B.extra.Currency;
import com.B2B.extra.RejectionCause;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentRejectedV1 extends BaseEvent
{
    private final UUID receiverAccountId;
    private final UUID senderAccountId;
    private final BigDecimal amount;
    private final Currency currency;
    private final Instant requestedAt;
    private final Instant rejectedAt;
    private final String paymentCause;
    private final RejectionCause rejectionCause;


    public PaymentRejectedV1(UUID eventId, UUID receiverAccountId, UUID senderAccountId, BigDecimal amount, Currency currency, Instant requestedAt, Instant rejectedAt, String paymentCause, RejectionCause rejectionCause)
    {
        super(eventId, Instant.now());
        this.receiverAccountId = receiverAccountId;
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
