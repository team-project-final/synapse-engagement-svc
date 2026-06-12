package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import com.synapse.engagement.shared.KafkaTopicResolver;
import com.synapse.engagement.BadgeEarned;
import com.synapse.engagement.LevelUp;
import com.synapse.platform.NotificationSend;
import org.apache.avro.specific.SpecificRecord;
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
    private static final String LEVEL_UP_NOTIFICATION_ID_PREFIX = "level-up:";
    private static final String LEVEL_UP_NOTIFICATION_TYPE = "LEVEL_UP";
    private static final String FCM_CHANNEL = "FCM";

    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;
    private final String levelUpTopic;
    private final String badgeEarnedTopic;
    private final String notificationSendTopic;
    private final Clock clock;

    public GamificationKafkaProducer(
            KafkaTemplate<String, SpecificRecord> kafkaTemplate,
            KafkaTopicResolver kafkaTopicResolver
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.levelUpTopic = kafkaTopicResolver.levelUp();
        this.badgeEarnedTopic = kafkaTopicResolver.badgeEarned();
        this.notificationSendTopic = kafkaTopicResolver.notificationSend();
        this.clock = Clock.systemUTC();
    }

    @Override
    public void publishLevelUp(Long userId, String externalUserId, String tenantId, int oldLevel, int newLevel, int totalXp) {
        // 필드명과 타입은 synapse-shared/src/main/avro/engagement/LevelUp.avsc와 반드시 맞춘다.
        // userId는 platform UUID(externalUserId)를 그대로 싣는다 — platform이 UUID.fromString(userId) 한다(F10).
        var event = LevelUp.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTenantId(tenantId)
                .setUserId(externalUserId)
                .setPreviousLevel(oldLevel)
                .setNewLevel(newLevel)
                .setTotalXp((long) totalXp)
                .setOccurredAt(clock.millis())
                .build();
        send(levelUpTopic, tenantId, event);
        // 레벨업은 사용자 알림 대상 — platform 알림 버스(notification-send)로도 발행한다(W1 알림 leg, F10).
        send(notificationSendTopic, tenantId, levelUpNotification(externalUserId, tenantId, newLevel, totalXp));
    }

    private NotificationSend levelUpNotification(String externalUserId, String tenantId, int newLevel, int totalXp) {
        // eventId는 (externalUserId, newLevel) 기반 결정적 UUID — 재전달돼도 platform 알림 dedupe(eventId)가 작동.
        var eventId = UUID.nameUUIDFromBytes(
                (LEVEL_UP_NOTIFICATION_ID_PREFIX + externalUserId + ":" + newLevel).getBytes(StandardCharsets.UTF_8)).toString();
        return NotificationSend.newBuilder()
                .setEventId(eventId)
                .setTenantId(tenantId)
                .setOccurredAt(clock.millis())
                // platform NotificationService가 UUID.fromString(userId) 하므로 UUID를 그대로 싣는다(F10).
                .setUserId(externalUserId)
                .setNotificationType(LEVEL_UP_NOTIFICATION_TYPE)
                .setChannels(List.of(FCM_CHANNEL))
                .setTitle("레벨 업!")
                .setBody("레벨 " + newLevel + " 달성 (XP " + totalXp + ")")
                .build();
    }

    @Override
    public void publishBadgeEarned(Long userId, String externalUserId, String tenantId, BadgeResponse badge) {
        // 별도 badge UUID가 생기기 전까지 badgeId는 안정적인 로컬 badge code로 매핑한다.
        // userId는 platform UUID(externalUserId)를 그대로 싣는다(F10).
        var event = BadgeEarned.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTenantId(tenantId)
                .setUserId(externalUserId)
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
