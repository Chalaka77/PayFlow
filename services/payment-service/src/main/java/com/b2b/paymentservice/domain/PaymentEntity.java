package com.b2b.paymentservice.domain;

import com.B2B.extra.Currency;
import com.B2B.extra.StatusPayment;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
public class PaymentEntity
{
    @Id
    @UuidGenerator
    private UUID paymentId;

    @Column(nullable = false, updatable = false)
    private UUID senderAccountId;

    @Column(nullable = false, updatable = false)
    private UUID receiverAccountId;

    @Column(nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusPayment status;

    @Column(nullable = false,  updatable = false)
    private Instant paymentRequestedAt;

    @Column(nullable = true,  updatable = false)
    private String reasonPayment;

    @Column(nullable = true)
    private Instant updatedAt;

    @Column(nullable = true)
    private String rejectionCause;


    public PaymentEntity(UUID senderAccountId, UUID receiverAccountId, BigDecimal amount, Currency currency, StatusPayment status, Instant paymentRequestedAt)
    {
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.paymentRequestedAt = paymentRequestedAt;
    }

    protected PaymentEntity()
    {}

    public void updateStatus(StatusPayment newStatus)
    {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void setRejectionCause(String rejectionCause)
    {
        this.rejectionCause = rejectionCause;
    }

    public String getRejectionCause()
    {
        return this.rejectionCause;
    }

    public void setReasonPayment(String reasonPayment)
    {
        this.reasonPayment = reasonPayment;
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

    public StatusPayment getStatus()
    {
        return status;
    }

    public Instant getPaymentRequestedAt()
    {
        return paymentRequestedAt;
    }

    public String getReasonPayment()
    {
        return reasonPayment;
    }

    public Instant getUpdatedAt()
    {
        return updatedAt;
    }
}
