package io.synapse.gamification.dto;

import io.synapse.gamification.entity.EventType;
import java.time.LocalDateTime;
import java.util.UUID;

public record XpEventResponse(
        UUID id,
        EventType eventType,
        int xpAmount,
        String sourceId,
        String sourceType,
        LocalDateTime createdAt) {
}
