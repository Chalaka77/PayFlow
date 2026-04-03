package com.b2b.notificationservice.domain;

import com.B2B.extra.StatusPayment;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification")
public class NotificationEntity
{
    @Id
    @UuidGenerator
    private UUID notificationId;

    @Column(nullable = true)
    private UUID paymentId;
    @Column(nullable = false)
    private UUID accountId;
    @Column(nullable = false)
    private String message;
    @Column(nullable = false)
    private Instant sentAt;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusPayment statusPayment;

    public NotificationEntity(UUID accountId, String message, Instant sentAt, StatusPayment statusPayment)
    {
        this.accountId = accountId;
        this.message = message;
        this.sentAt = sentAt;
        this.statusPayment = statusPayment;
    }

    protected NotificationEntity(){}

    public void setPaymentId(UUID paymentId)
    {
        this.paymentId = paymentId;
    }

    public UUID getNotificationId()
    {
        return notificationId;
    }

    public UUID getPaymentId()
    {
        return paymentId;
    }

    public UUID getAccountId()
    {
        return accountId;
    }

    public String getMessage()
    {
        return message;
    }

    public Instant getSentAt()
    {
        return sentAt;
    }

    public StatusPayment getStatusPayment()
    {
        return statusPayment;
    }
}
