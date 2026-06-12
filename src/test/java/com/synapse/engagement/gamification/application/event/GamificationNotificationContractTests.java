package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.BadgeEarned;
import com.synapse.engagement.LevelUp;
import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import com.synapse.engagement.gamification.domain.BadgeConditionType;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.specific.SpecificRecord;
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
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.properties.schema.registry.url=mock://gamification-notification-contract",
        "synapse.kafka.topics.level-up=engagement.gamification.level-up-v1",
        "synapse.kafka.topics.badge-earned=engagement.gamification.badge-earned-v1"
})
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "engagement.gamification.level-up-v1",
                "engagement.gamification.badge-earned-v1"
        }
)
class GamificationNotificationContractTests {
    @Autowired
    private GamificationEventPublisher publisher;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void fakeNotificationProcessorCanConsumeGamificationAvroEvents() {
        var consumerProps = KafkaTestUtils.consumerProps("fake-notification-svc-group", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://gamification-notification-contract");
        consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new KafkaAvroDeserializer()
        );
        var fakeNotificationProcessor = new FakeNotificationProcessor();

        try (var consumer = consumerFactory.createConsumer()) {
            embeddedKafka.consumeFromEmbeddedTopics(
                    consumer,
                    "engagement.gamification.level-up-v1",
                    "engagement.gamification.badge-earned-v1"
            );

            var externalUserId = "44444444-4444-4444-4444-444444444444";
            publisher.publishLevelUp(90L, externalUserId, "tenant-notify", 2, 3, 360);
            publisher.publishBadgeEarned(
                    90L,
                    externalUserId,
                    "tenant-notify",
                    new BadgeResponse(
                            "STREAK_3",
                            "3 Day Streak",
                            "Keep a 3 day activity streak",
                            null,
                            BadgeConditionType.STREAK,
                            3,
                            Instant.now()
                    )
            );

            var levelUpRecord = KafkaTestUtils.getSingleRecord(
                    consumer,
                    "engagement.gamification.level-up-v1"
            );
            var badgeEarnedRecord = KafkaTestUtils.getSingleRecord(
                    consumer,
                    "engagement.gamification.badge-earned-v1"
            );

            var levelUpNotification = fakeNotificationProcessor.toNotification(
                    levelUpRecord.key(),
                    (SpecificRecord) levelUpRecord.value()
            );
            var badgeNotification = fakeNotificationProcessor.toNotification(
                    badgeEarnedRecord.key(),
                    (SpecificRecord) badgeEarnedRecord.value()
            );

            assertThat(levelUpNotification).isEqualTo(new NotificationCommand(
                    "tenant-notify",
                    externalUserId,
                    "LEVEL_UP",
                    "Level 3 reached",
                    "totalXp=360"
            ));
            assertThat(badgeNotification).isEqualTo(new NotificationCommand(
                    "tenant-notify",
                    externalUserId,
                    "BADGE_EARNED",
                    "Badge earned: 3 Day Streak",
                    "badgeCode=STREAK_3"
            ));
        }
    }

    private record NotificationCommand(
            String tenantId,
            String userId,
            String notificationType,
            String title,
            String body
    ) {
    }

    private static class FakeNotificationProcessor {
        NotificationCommand toNotification(String partitionKey, SpecificRecord event) {
            if (event instanceof LevelUp levelUp) {
                return new NotificationCommand(
                        partitionKey,
                        levelUp.getUserId().toString(),
                        "LEVEL_UP",
                        "Level " + levelUp.getNewLevel() + " reached",
                        "totalXp=" + levelUp.getTotalXp()
                );
            }
            if (event instanceof BadgeEarned badgeEarned) {
                return new NotificationCommand(
                        partitionKey,
                        badgeEarned.getUserId().toString(),
                        "BADGE_EARNED",
                        "Badge earned: " + badgeEarned.getBadgeName(),
                        "badgeCode=" + badgeEarned.getBadgeCode()
                );
            }
            throw new IllegalArgumentException("Unsupported gamification event: " + event.getSchema().getFullName());
        }
    }
}
