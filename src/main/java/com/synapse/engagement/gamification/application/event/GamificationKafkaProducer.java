package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import com.synapse.engagement.BadgeEarned;
import com.synapse.engagement.LevelUp;
import com.synapse.platform.NotificationSend;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "synapse.kafka", name = "enabled", havingValue = "true")
public class GamificationKafkaProducer implements GamificationEventPublisher {
    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;
    private final String levelUpTopic;
    private final String badgeEarnedTopic;
    private final String notificationSendTopic;
    private final Clock clock;

    public GamificationKafkaProducer(
            KafkaTemplate<String, SpecificRecord> kafkaTemplate,
            @Value("${synapse.kafka.topics.level-up}") String levelUpTopic,
            @Value("${synapse.kafka.topics.badge-earned}") String badgeEarnedTopic,
            @Value("${synapse.kafka.topics.notification-send}") String notificationSendTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.levelUpTopic = levelUpTopic;
        this.badgeEarnedTopic = badgeEarnedTopic;
        this.notificationSendTopic = notificationSendTopic;
        this.clock = Clock.systemUTC();
    }

    @Override
    public void publishLevelUp(Long userId, String tenantId, int oldLevel, int newLevel, int totalXp) {
        // 필드명과 타입은 synapse-shared/src/main/avro/engagement/LevelUp.avsc와 반드시 맞춘다.
        var event = LevelUp.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTenantId(tenantId)
                .setUserId(String.valueOf(userId))
                .setPreviousLevel(oldLevel)
                .setNewLevel(newLevel)
                .setTotalXp((long) totalXp)
                .setOccurredAt(clock.millis())
                .build();
        send(levelUpTopic, tenantId, event);
        // 레벨업은 사용자 알림 대상 — platform 알림 버스(notification-send)로도 발행한다(W1 알림 leg, F10).
        send(notificationSendTopic, tenantId, levelUpNotification(userId, tenantId, newLevel, totalXp));
    }

    private NotificationSend levelUpNotification(Long userId, String tenantId, int newLevel, int totalXp) {
        // eventId는 (userId, newLevel) 기반 결정적 UUID — 재전달돼도 platform 알림 dedupe(eventId)가 작동.
        var eventId = UUID.nameUUIDFromBytes(
                ("level-up:" + userId + ":" + newLevel).getBytes(StandardCharsets.UTF_8)).toString();
        return NotificationSend.newBuilder()
                .setEventId(eventId)
                .setTenantId(tenantId)
                .setOccurredAt(clock.millis())
                .setUserId(String.valueOf(userId))
                .setNotificationType("LEVEL_UP")
                .setChannels(List.of("FCM"))
                .setTitle("레벨 업!")
                .setBody("레벨 " + newLevel + " 달성 (XP " + totalXp + ")")
                .build();
    }

    @Override
    public void publishBadgeEarned(Long userId, String tenantId, BadgeResponse badge) {
        // 별도 badge UUID가 생기기 전까지 badgeId는 안정적인 로컬 badge code로 매핑한다.
        var event = BadgeEarned.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTenantId(tenantId)
                .setUserId(String.valueOf(userId))
                .setBadgeId(badge.code())
                .setBadgeCode(badge.code())
                .setBadgeName(badge.name())
                .setOccurredAt(clock.millis())
                .build();
        send(badgeEarnedTopic, tenantId, event);
    }

    private void send(String topic, String tenantId, SpecificRecord event) {
        // EVENT_CONTRACT_STANDARD는 tenant 단위 순서를 위해 tenantId를 partition key로 사용한다.
        kafkaTemplate.send(topic, tenantId, event);
    }
}
