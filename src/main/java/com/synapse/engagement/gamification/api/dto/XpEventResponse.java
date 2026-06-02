package com.synapse.engagement.gamification.api.dto;

import com.synapse.engagement.gamification.domain.EventType;
import com.synapse.engagement.gamification.domain.XpEvent;

import java.time.Instant;

public record XpEventResponse(
        EventType eventType,
        int xpAmount,
        String sourceId,
        String sourceType,
        String eventId,
        Instant createdAt
) {
    public static XpEventResponse from(XpEvent event) {
        return new XpEventResponse(
                event.getEventType(),
                event.getXpAmount(),
                event.getSourceId(),
                event.getSourceType(),
                event.getEventId(),
                event.getCreatedAt()
        );
    }
}
