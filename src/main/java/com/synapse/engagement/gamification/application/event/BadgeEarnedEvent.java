package com.synapse.engagement.gamification.application.event;

import java.time.Instant;

public record BadgeEarnedEvent(
        Long userId,
        String tenantId,
        String badgeId,
        String badgeName,
        Instant occurredAt
) {
}
