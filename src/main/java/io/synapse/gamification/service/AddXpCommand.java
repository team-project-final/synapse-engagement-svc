package io.synapse.gamification.service;

import io.synapse.gamification.entity.EventType;
import java.util.UUID;

public record AddXpCommand(
        UUID userId,
        EventType eventType,
        int xpAmount,
        String sourceId,
        String sourceType,
        String eventId) {
}
