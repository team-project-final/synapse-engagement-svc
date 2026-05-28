package com.synapse.engagement.gamification.application.event;

import java.time.Instant;

public record LevelUpEvent(
        Long userId,
        String tenantId,
        int oldLevel,
        int newLevel,
        int totalXp,
        Instant occurredAt
) {
}
