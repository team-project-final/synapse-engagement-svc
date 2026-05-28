package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "synapse.kafka", name = "enabled", havingValue = "true")
public class GamificationKafkaProducer implements GamificationEventPublisher {
    static final String LEVEL_UP_TYPE = "com.synapse.event.engagement.GamificationLevelUp";
    static final String BADGE_EARNED_TYPE = "com.synapse.event.engagement.GamificationBadgeEarned";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final String levelUpTopic;
    private final String badgeEarnedTopic;

    public GamificationKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${synapse.kafka.topics.level-up}") String levelUpTopic,
            @Value("${synapse.kafka.topics.badge-earned}") String badgeEarnedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = new JsonMapper();
        this.levelUpTopic = levelUpTopic;
        this.badgeEarnedTopic = badgeEarnedTopic;
    }

    @Override
    public void publishLevelUp(Long userId, String tenantId, int oldLevel, int newLevel, int totalXp) {
        LevelUpEvent event = new LevelUpEvent(userId, tenantId, oldLevel, newLevel, totalXp, Instant.now());
        send(levelUpTopic, userId, CloudEventEnvelope.create(UUID.randomUUID().toString(), LEVEL_UP_TYPE, tenantId, event));
    }

    @Override
    public void publishBadgeEarned(Long userId, String tenantId, BadgeResponse badge) {
        BadgeEarnedEvent event = new BadgeEarnedEvent(userId, tenantId, badge.code(), badge.name(), Instant.now());
        send(badgeEarnedTopic, userId, CloudEventEnvelope.create(UUID.randomUUID().toString(), BADGE_EARNED_TYPE, tenantId, event));
    }

    private void send(String topic, Long userId, CloudEventEnvelope<?> envelope) {
        try {
            kafkaTemplate.send(topic, String.valueOf(userId), jsonMapper.writeValueAsString(envelope));
        } catch (Exception ex) {
            throw new IllegalStateException("gamification Kafka 이벤트 직렬화에 실패했습니다", ex);
        }
    }
}
