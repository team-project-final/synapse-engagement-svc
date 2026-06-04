package com.synapse.engagement.community.application.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "synapse.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
class NoopCommunityNotificationPublisher implements CommunityNotificationPublisher {
    @Override
    public void publishModerationNotification(Long recipientUserId, String tenantId, String notificationType,
                                             String title, String body, Map<String, String> data) {
        // Kafka 비활성(dev/test 기본값) 시 비즈니스 로직만 수행한다.
    }
}
