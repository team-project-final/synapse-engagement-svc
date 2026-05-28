package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import com.synapse.engagement.gamification.domain.BadgeConditionType;
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
class GamificationKafkaProducerTests {
    @Autowired
    private GamificationEventPublisher publisher;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void publishesLevelUpAndBadgeEarnedCloudEvents() {
        var consumerProps = KafkaTestUtils.consumerProps("gamification-step7-test", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new StringDeserializer()
        );

        try (var consumer = consumerFactory.createConsumer()) {
            embeddedKafka.consumeFromEmbeddedTopics(
                    consumer,
                    "engagement.gamification.level-up-v1",
                    "engagement.gamification.badge-earned-v1"
            );

            publisher.publishLevelUp(80L, "tenant-a", 1, 2, 120);
            var badge = new BadgeResponse(
                    "LEVEL_2",
                    "Level 2",
                    "Reach level 2",
                    null,
                    BadgeConditionType.LEVEL,
                    2,
                    Instant.now()
            );
            publisher.publishBadgeEarned(80L, "tenant-a", badge);

            var levelUp = KafkaTestUtils.getSingleRecord(
                    consumer,
                    "engagement.gamification.level-up-v1"
            );
            var badgeEarned = KafkaTestUtils.getSingleRecord(
                    consumer,
                    "engagement.gamification.badge-earned-v1"
            );

            assertThat(levelUp.key()).isEqualTo("80");
            assertThat(levelUp.value())
                    .contains("\"specversion\":\"1.0\"")
                    .contains("\"tenantid\":\"tenant-a\"")
                    .contains("\"tenantId\":\"tenant-a\"")
                    .contains("\"type\":\"com.synapse.event.engagement.GamificationLevelUp\"")
                    .contains("\"oldLevel\":1")
                    .contains("\"newLevel\":2");

            assertThat(badgeEarned.key()).isEqualTo("80");
            assertThat(badgeEarned.value())
                    .contains("\"specversion\":\"1.0\"")
                    .contains("\"tenantid\":\"tenant-a\"")
                    .contains("\"tenantId\":\"tenant-a\"")
                    .contains("\"type\":\"com.synapse.event.engagement.GamificationBadgeEarned\"")
                    .contains("\"badgeId\":\"LEVEL_2\"")
                    .contains("\"badgeName\":\"Level 2\"");
        }
    }
}
