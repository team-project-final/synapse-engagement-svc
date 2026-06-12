package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.BadgeEarned;
import com.synapse.engagement.LevelUp;
import com.synapse.platform.NotificationSend;
import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import com.synapse.engagement.gamification.domain.BadgeConditionType;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "synapse.kafka.enabled=true",
        "synapse.kafka.topic-prefix=dev.",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.properties.schema.registry.url=mock://gamification-step7",
        "synapse.kafka.topics.level-up=engagement.gamification.level-up-v1",
        "synapse.kafka.topics.badge-earned=engagement.gamification.badge-earned-v1",
        "synapse.kafka.topics.notification-send=platform.notification.notification-send-v1"
})
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "dev.platform.auth.user-registered-v1",
                "dev.learning.card.review-completed-v1",
                "dev.engagement.gamification.level-up-v1",
                "dev.engagement.gamification.badge-earned-v1",
                "dev.platform.notification.notification-send-v1"
        }
)
class GamificationKafkaProducerTests {
    @Autowired
    private GamificationEventPublisher publisher;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void publishesLevelUpAndBadgeEarnedAvroRecords() {
        var consumerProps = KafkaTestUtils.consumerProps("gamification-step7-test", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // 런타임과 같은 serializer 경로를 검증하기 위해 Confluent in-memory registry를 사용한다.
        consumerProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://gamification-step7");
        consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new KafkaAvroDeserializer()
        );

        try (var consumer = consumerFactory.createConsumer()) {
            embeddedKafka.consumeFromEmbeddedTopics(
                    consumer,
                    "dev.engagement.gamification.level-up-v1",
                    "dev.engagement.gamification.badge-earned-v1",
                    "dev.platform.notification.notification-send-v1"
            );

            // outbound 이벤트는 platform UUID userId를 그대로 실어야 한다(F10): 내부 Long(80L)이 아니라 externalUserId.
            var externalUserId = "11111111-1111-1111-1111-111111111111";
            var tenantId = "22222222-2222-2222-2222-222222222222";
            publisher.publishLevelUp(80L, externalUserId, tenantId, 1, 2, 120);
            var badge = new BadgeResponse(
                    "LEVEL_2",
                    "Level 2",
                    "Reach level 2",
                    null,
                    BadgeConditionType.LEVEL,
                    2,
                    Instant.now()
            );
            publisher.publishBadgeEarned(80L, externalUserId, tenantId, badge);

            var levelUp = KafkaTestUtils.getSingleRecord(
                    consumer,
                    "dev.engagement.gamification.level-up-v1"
            );
            var badgeEarned = KafkaTestUtils.getSingleRecord(
                    consumer,
                    "dev.engagement.gamification.badge-earned-v1"
            );

            assertThat(levelUp.key()).isEqualTo(tenantId);
            assertThat(levelUp.value()).isInstanceOf(LevelUp.class);
            var levelUpEvent = (LevelUp) levelUp.value();
            assertThat(levelUpEvent.getEventId()).isNotBlank();
            assertThat(levelUpEvent.getTenantId()).isEqualTo(tenantId);
            assertThat(levelUpEvent.getUserId()).isEqualTo(externalUserId);
            assertThat(levelUpEvent.getPreviousLevel()).isEqualTo(1);
            assertThat(levelUpEvent.getNewLevel()).isEqualTo(2);
            assertThat(levelUpEvent.getTotalXp()).isEqualTo(120L);
            assertThat(levelUpEvent.getOccurredAt()).isPositive();

            assertThat(badgeEarned.key()).isEqualTo(tenantId);
            assertThat(badgeEarned.value()).isInstanceOf(BadgeEarned.class);
            var badgeEarnedEvent = (BadgeEarned) badgeEarned.value();
            assertThat(badgeEarnedEvent.getEventId()).isNotBlank();
            assertThat(badgeEarnedEvent.getTenantId()).isEqualTo(tenantId);
            assertThat(badgeEarnedEvent.getUserId()).isEqualTo(externalUserId);
            assertThat(badgeEarnedEvent.getBadgeId()).isEqualTo("LEVEL_2");
            assertThat(badgeEarnedEvent.getBadgeCode()).isEqualTo("LEVEL_2");
            assertThat(badgeEarnedEvent.getBadgeName()).isEqualTo("Level 2");
            assertThat(badgeEarnedEvent.getOccurredAt()).isPositive();

            // 레벨업 시 platform 알림 버스로 NotificationSend도 발행되어야 한다 (F10, W1 알림 leg).
            var notification = KafkaTestUtils.getSingleRecord(
                    consumer,
                    "dev.platform.notification.notification-send-v1"
            );
            assertThat(notification.key()).isEqualTo(tenantId);
            assertThat(notification.value()).isInstanceOf(NotificationSend.class);
            var notificationEvent = (NotificationSend) notification.value();
            assertThat(notificationEvent.getEventId()).isNotBlank();
            assertThat(notificationEvent.getTenantId()).isEqualTo(tenantId);
            // platform NotificationService가 UUID.fromString(userId) 하므로 UUID여야 한다(F10 핵심).
            assertThat(notificationEvent.getUserId()).isEqualTo(externalUserId);
            assertThat(notificationEvent.getNotificationType().toString()).isEqualTo("LEVEL_UP");
            assertThat(notificationEvent.getOccurredAt()).isPositive();
        }
    }
}
