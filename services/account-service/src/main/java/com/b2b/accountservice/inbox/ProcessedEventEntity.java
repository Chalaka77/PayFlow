package com.b2b.accountservice.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_event")
public class ProcessedEventEntity
{
    @Id
    @Column(unique = true, nullable = false)
    private UUID eventId;
    @Column(nullable = false)
    private Instant processedAt;

    public ProcessedEventEntity(UUID eventId)
    {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }

    protected ProcessedEventEntity() {}

    public UUID getEventId()
    {
        return eventId;
    }

    public Instant getProcessedAt()
    {
        return processedAt;
    }
}
