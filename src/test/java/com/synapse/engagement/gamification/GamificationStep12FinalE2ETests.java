package com.synapse.engagement.gamification;

import com.synapse.engagement.BadgeEarned;
import com.synapse.engagement.LevelUp;
import com.synapse.engagement.gamification.domain.UserProfilesGamification;
import com.synapse.engagement.gamification.repository.UserProfilesGamificationRepository;
import com.synapse.engagement.gamification.repository.XpEventRepository;
import com.synapse.engagement.support.TestJwt;
import com.synapse.learning.Rating;
import com.synapse.learning.ReviewCompleted;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:gamification-step12;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "synapse.kafka.enabled=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.properties.schema.registry.url=mock://gamification-step12",
        "spring.kafka.consumer.group-id=engagement-step12-test-group",
        "synapse.kafka.topics.review-completed=learning.card.review-completed-v1",
        "synapse.kafka.topics.level-up=engagement.gamification.level-up-v1",
        "synapse.kafka.topics.badge-earned=engagement.gamification.badge-earned-v1"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "learning.card.review-completed-v1",
                "engagement.gamification.level-up-v1",
                "engagement.gamification.badge-earned-v1",
                "learning.card.review-completed-v1.dlq"
        }
)
class GamificationStep12FinalE2ETests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private KafkaTemplate<String, SpecificRecord> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private UserProfilesGamificationRepository profileRepository;

    @Autowired
    private XpEventRepository xpEventRepository;

    @Test
    void reviewToXpLevelBadgeLeaderboardAndNotificationFlowWorksEndToEnd() throws Exception {
        var userId = 12000L;
        var tenantId = "tenant-step12";
        var eventId = "review-completed:step12-card-1:2026-06-08T00:00:00Z";
        var token = "Bearer " + TestJwt.accessToken(String.valueOf(userId));

        var profile = UserProfilesGamification.initialize(userId);
        profile.addXp(90, 1);
        profileRepository.save(profile);

        var consumerProps = KafkaTestUtils.consumerProps("step12-fake-notification-svc", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://gamification-step12");
        consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new KafkaAvroDeserializer()
        );
        var fakeNotificationProcessor = new FakeNotificationProcessor();

        try (var outputConsumer = consumerFactory.createConsumer()) {
            embeddedKafka.consumeFromEmbeddedTopics(
                    outputConsumer,
                    "engagement.gamification.level-up-v1",
                    "engagement.gamification.badge-earned-v1"
            );

            publishReviewCompleted(userId, tenantId);

            assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
                while (!xpEventRepository.existsByEventId(eventId)) {
                    Thread.sleep(100L);
                }
            });

            var savedProfile = profileRepository.findById(userId).orElseThrow();
            assertThat(savedProfile.getTotalXp()).isEqualTo(100);
            assertThat(savedProfile.getLevel()).isEqualTo(2);
            assertThat(xpEventRepository.findByUserIdOrderByCreatedAtDesc(userId)).hasSize(1);

            var outputEvents = pollRecords(outputConsumer, 3);
            assertThat(outputEvents).anySatisfy(event -> {
                assertThat(event).isInstanceOf(LevelUp.class);
                var levelUp = (LevelUp) event;
                assertThat(levelUp.getTenantId()).isEqualTo(tenantId);
                assertThat(levelUp.getUserId()).isEqualTo(String.valueOf(userId));
                assertThat(levelUp.getPreviousLevel()).isEqualTo(1);
                assertThat(levelUp.getNewLevel()).isEqualTo(2);
                assertThat(levelUp.getTotalXp()).isEqualTo(100L);
            });
            assertThat(outputEvents).anySatisfy(event -> {
                assertThat(event).isInstanceOf(BadgeEarned.class);
                var badgeEarned = (BadgeEarned) event;
                assertThat(badgeEarned.getTenantId()).isEqualTo(tenantId);
                assertThat(badgeEarned.getUserId()).isEqualTo(String.valueOf(userId));
                assertThat(badgeEarned.getBadgeCode()).isEqualTo("LEVEL_2");
            });

            var notificationCommands = outputEvents.stream()
                    .map(event -> fakeNotificationProcessor.toNotification(tenantId, event))
                    .toList();
            assertThat(notificationCommands).contains(
                    new NotificationCommand(tenantId, String.valueOf(userId), "LEVEL_UP", "Level 2 reached", "totalXp=100")
            );
            assertThat(notificationCommands).anySatisfy(command -> {
                assertThat(command.notificationType()).isEqualTo("BADGE_EARNED");
                assertThat(command.title()).isEqualTo("Badge earned: Level 2");
                assertThat(command.body()).isEqualTo("badgeCode=LEVEL_2");
            });

            mvc.perform(get("/api/v1/gamification/me")
                            .header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.xp").value(100))
                    .andExpect(jsonPath("$.level").value(2))
                    .andExpect(jsonPath("$.badges[*].code", hasItem("FIRST_XP")))
                    .andExpect(jsonPath("$.badges[*].code", hasItem("LEVEL_2")));

            mvc.perform(get("/api/v1/gamification/xp/history")
                            .header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].eventId", hasItem(eventId)));

            mvc.perform(get("/api/v1/gamification/leaderboard?limit=5")
                            .header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].userId").value(userId))
                    .andExpect(jsonPath("$[0].xp").value(100))
                    .andExpect(jsonPath("$[0].level").value(2));

            publishReviewCompleted(userId, tenantId);
            Thread.sleep(500L);

            assertThat(xpEventRepository.findByUserIdOrderByCreatedAtDesc(userId)).hasSize(1);
            var profileAfterDuplicate = profileRepository.findById(userId).orElseThrow();
            assertThat(profileAfterDuplicate.getTotalXp()).isEqualTo(100);
            assertThat(profileAfterDuplicate.getLevel()).isEqualTo(2);
        }
    }

    private void publishReviewCompleted(Long userId, String tenantId) {
        kafkaTemplate.send(
                "learning.card.review-completed-v1",
                tenantId,
                new ReviewCompleted(
                        "step12-card-1",
                        String.valueOf(userId),
                        tenantId,
                        Rating.GOOD,
                        "2026-06-09T00:00:00Z",
                        "2026-06-08T00:00:00Z"
                )
        );
        kafkaTemplate.flush();
    }

    private List<SpecificRecord> pollRecords(Consumer<String, Object> consumer, int expectedCount) {
        return assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            var records = new ArrayList<SpecificRecord>();
            while (records.size() < expectedCount) {
                var polled = KafkaTestUtils.getRecords(consumer, Duration.ofMillis(500));
                StreamSupport.stream(polled.spliterator(), false)
                        .map(record -> (SpecificRecord) record.value())
                        .forEach(records::add);
            }
            return records;
        });
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
