package com.b2b.paymentservice.outbox;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox", indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
public class OutboxEventEntity
{
    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private String topic;

    @Column(nullable = false, updatable = false)
    private String eventType;

    @Column(nullable = false, updatable = false)
    private String aggregateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, updatable = false)
    private String payloadJson;

    @Column(nullable = false)
    private String status;

    private Instant sentAt;

    protected OutboxEventEntity() {}

    public OutboxEventEntity(String topic, String eventType, String aggregateId, String payloadJson)
    {
        this.createdAt = Instant.now();
        this.topic = topic;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payloadJson = payloadJson;
        this.status = "NEW";
    }

    public void markAsSent()
    {
        this.status = "SENT";
        this.sentAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public String getTopic() { return topic; }
    public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; }
    public String getPayloadJson() { return payloadJson; }
    public String getStatus() { return status; }
    public Instant getSentAt() { return sentAt; }
}