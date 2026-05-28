package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.gamification.api.dto.BadgeResponse;

public interface GamificationEventPublisher {
    void publishLevelUp(Long userId, String tenantId, int oldLevel, int newLevel, int totalXp);

    void publishBadgeEarned(Long userId, String tenantId, BadgeResponse badge);
}
