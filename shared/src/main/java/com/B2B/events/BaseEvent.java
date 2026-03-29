package com.B2B.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public class BaseEvent
{
    private final UUID eventId;
    private final Instant eventOccurredAt;

    @JsonCreator
    public BaseEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("eventOccurredAt") Instant eventOccurredAt
    )
    {
        this.eventId = eventId;
        this.eventOccurredAt = eventOccurredAt;
    }

    public UUID getEventId() { return eventId; }

    public Instant getEventOccurredAt() { return eventOccurredAt; }
}
