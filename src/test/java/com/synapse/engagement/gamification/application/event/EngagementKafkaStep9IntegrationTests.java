package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.BadgeEarned;
import com.synapse.engagement.LevelUp;
import com.synapse.engagement.gamification.domain.Badge;
import com.synapse.engagement.gamification.domain.BadgeConditionType;
import com.synapse.engagement.gamification.domain.EventType;
import com.synapse.engagement.gamification.domain.UserBadge;
import com.synapse.engagement.gamification.domain.UserProfilesGamification;
import com.synapse.engagement.gamification.repository.BadgeRepository;
import com.synapse.engagement.gamification.repository.UserBadgeRepository;
import com.synapse.engagement.gamification.repository.UserProfilesGamificationRepository;
import com.synapse.engagement.gamification.repository.XpEventRepository;
import com.synapse.learning.Rating;
import com.synapse.learning.ReviewCompleted;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

@SpringBootTest(properties = {
        "synapse.kafka.enabled=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.properties.schema.registry.url=mock://engagement-step9",
        "spring.kafka.consumer.group-id=engagement-step9-test-group",
        "synapse.kafka.topics.review-completed=learning.card.review-completed-v1",
        "synapse.kafka.topics.level-up=engagement.gamification.level-up-v1",
        "synapse.kafka.topics.badge-earned=engagement.gamification.badge-earned-v1"
})
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
class EngagementKafkaStep9IntegrationTests {
    @Autowired
    private KafkaTemplate<String, SpecificRecord> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private UserProfilesGamificationRepository profileRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private XpEventRepository xpEventRepository;

    @Test
    void reviewCompletedConsumerAppliesXpAndPublishesGamificationEvents() {
        var userId = 900L;
        // 사용자를 90 XP / level 1 상태로 시작시킨다.
        // ReviewCompleted 기본 보상 10 XP가 들어오면 100 XP가 되어 level 2와 LEVEL_2 배지가 동시에 검증된다.
        var profile = UserProfilesGamification.initialize(userId);
        profile.addXp(90, 1);
        profileRepository.save(profile);
        // FIRST_XP는 이미 받은 것으로 만들어 이번 테스트에서 발행되는 badge-earned 이벤트가 LEVEL_2에 집중되게 한다.
        var firstXp = badgeRepository.save(Badge.create(
                "FIRST_XP",
                "First XP",
                "Earn XP for the first time",
                null,
                BadgeConditionType.TOTAL_XP,
                1
        ));
        userBadgeRepository.save(UserBadge.earn(userId, firstXp));

        var consumerProps = KafkaTestUtils.consumerProps("engagement-step9-output-test", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://engagement-step9");
        consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        // output consumer도 런타임과 같은 Avro deserializer를 써서 Schema Registry 경로까지 같이 검증한다.
        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new KafkaAvroDeserializer()
        );

        try (var outputConsumer = consumerFactory.createConsumer()) {
            // Step 9의 결과 토픽 두 개를 직접 읽어 Consumer 처리 후 Producer 발행까지 한 번에 확인한다.
            embeddedKafka.consumeFromEmbeddedTopics(
                    outputConsumer,
                    "engagement.gamification.level-up-v1",
                    "engagement.gamification.badge-earned-v1"
            );

            // learning-svc가 복습 완료 이벤트를 보낸 상황을 재현한다.
            // EngagementKafkaConsumer가 이 이벤트를 받아 기존 GamificationService.addXp 흐름으로 연결한다.
            kafkaTemplate.send(
                    "learning.card.review-completed-v1",
                    "tenant-step9",
                    new ReviewCompleted(
                            "card-step9",
                            String.valueOf(userId),
                            "tenant-step9",
                            Rating.GOOD,
                            "2026-06-05T00:00:00Z",
                            "2026-06-04T00:00:00Z"
                    )
            );
            kafkaTemplate.flush();

            // Kafka listener는 비동기로 동작하므로, XP 이벤트가 저장될 때까지 짧게 polling한다.
            assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
                while (!xpEventRepository.existsByEventId("review-completed:card-step9:2026-06-04T00:00:00Z")) {
                    Thread.sleep(100L);
                }
            });

            var savedProfile = profileRepository.findById(userId).orElseThrow();
            assertThat(savedProfile.getTotalXp()).isEqualTo(100);
            assertThat(savedProfile.getLevel()).isEqualTo(2);
            var xpEvent = xpEventRepository.findByUserIdOrderByCreatedAtDesc(userId).getFirst();
            assertThat(xpEvent.getEventType()).isEqualTo(EventType.CARD_REVIEWED);
            assertThat(xpEvent.getSourceType()).isEqualTo("card-review");

            // 최종적으로 XP 적립 결과가 downstream용 Avro 이벤트로 발행됐는지 검증한다.
            var records = KafkaTestUtils.getRecords(outputConsumer, Duration.ofSeconds(10));
            var values = StreamSupport.stream(records.spliterator(), false)
                    .map(record -> record.value())
                    .toList();

            assertThat(values).anySatisfy(value -> {
                assertThat(value).isInstanceOf(LevelUp.class);
                var levelUp = (LevelUp) value;
                assertThat(levelUp.getTenantId()).isEqualTo("tenant-step9");
                assertThat(levelUp.getUserId()).isEqualTo(String.valueOf(userId));
                assertThat(levelUp.getPreviousLevel()).isEqualTo(1);
                assertThat(levelUp.getNewLevel()).isEqualTo(2);
                assertThat(levelUp.getTotalXp()).isEqualTo(100L);
            });
            assertThat(values).anySatisfy(value -> {
                assertThat(value).isInstanceOf(BadgeEarned.class);
                var badgeEarned = (BadgeEarned) value;
                assertThat(badgeEarned.getTenantId()).isEqualTo("tenant-step9");
                assertThat(badgeEarned.getUserId()).isEqualTo(String.valueOf(userId));
                assertThat(badgeEarned.getBadgeCode()).isEqualTo("LEVEL_2");
            });
        }
    }
}
