package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "synapse.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
class NoopGamificationEventPublisher implements GamificationEventPublisher {
    @Override
    public void publishLevelUp(Long userId, String tenantId, int oldLevel, int newLevel, int totalXp) {
        // Kafka 비활성화(dev/test 기본값) 시 비즈니스 로직만 수행한다.
    }

    @Override
    public void publishBadgeEarned(Long userId, String tenantId, BadgeResponse badge) {
        // Kafka 비활성화(dev/test 기본값) 시 비즈니스 로직만 수행한다.
    }
}
