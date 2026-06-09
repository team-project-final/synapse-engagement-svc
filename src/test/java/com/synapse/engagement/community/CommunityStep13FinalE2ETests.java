package com.synapse.engagement.community;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.engagement.support.TestJwt;
import com.synapse.platform.NotificationSend;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:community-step13;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "synapse.kafka.enabled=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.properties.schema.registry.url=mock://community-step13",
        "spring.kafka.consumer.group-id=community-step13-test-group",
        "synapse.kafka.topics.user-registered=platform.auth.user-registered-v1",
        "synapse.kafka.topics.review-completed=learning.card.review-completed-v1",
        "synapse.kafka.topics.level-up=engagement.gamification.level-up-v1",
        "synapse.kafka.topics.badge-earned=engagement.gamification.badge-earned-v1",
        "synapse.kafka.topics.notification-send=platform.notification.notification-send-v1"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "platform.auth.user-registered-v1",
                "learning.card.review-completed-v1",
                "engagement.gamification.level-up-v1",
                "engagement.gamification.badge-earned-v1",
                "platform.notification.notification-send-v1"
        }
)
class CommunityStep13FinalE2ETests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sharedNoteReportModerationAndNotificationFlowWorksEndToEnd() throws Exception {
        var ownerToken = bearer("13100");
        var reporterToken = bearer("13101");
        var adminToken = bearer("13900", List.of("ADMIN"));

        var note = json(mvc.perform(post("/api/v1/community/share")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"NOTE","contentId":93001,"title":"Step13 Note Alpha","description":"Step13 searchable note","tags":["step13","note"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shareToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString());
        var noteToken = note.get("shareToken").asText();

        var noteDetail = json(mvc.perform(get("/api/v1/community/share/" + noteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(13100))
                .andExpect(jsonPath("$.contentType").value("NOTE"))
                .andExpect(jsonPath("$.title").value("Step13 Note Alpha"))
                .andExpect(jsonPath("$.tags[*]", hasItem("note")))
                .andReturn().getResponse().getContentAsString());
        var noteId = noteDetail.get("id").asLong();

        mvc.perform(get("/api/v1/community/search?q=Step13&contentType=NOTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Step13 Note Alpha")));

        var notificationConsumer = notificationConsumer();
        try (notificationConsumer) {
            embeddedKafka.consumeFromEmbeddedTopics(
                    notificationConsumer,
                    "platform.notification.notification-send-v1"
            );

            mvc.perform(post("/api/v1/community/reports")
                            .header("Authorization", reporterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"targetType":"SHARED_NOTE","targetId":%d,"reason":"Step13 note moderation target"}
                                    """.formatted(noteId)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.targetType").value("SHARED_NOTE"))
                    .andExpect(jsonPath("$.targetId").value(noteId))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.reporterId").doesNotExist());

            mvc.perform(post("/api/v1/community/reports")
                            .header("Authorization", reporterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"targetType":"SHARED_NOTE","targetId":%d,"reason":"duplicate"}
                                    """.formatted(noteId)))
                    .andExpect(status().isConflict());

            mvc.perform(get("/api/v1/admin/reports")
                            .header("Authorization", reporterToken))
                    .andExpect(status().isForbidden());

            var pendingReports = json(mvc.perform(get("/api/v1/admin/reports?status=PENDING")
                            .header("Authorization", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].targetId", hasItem((int) noteId)))
                    .andReturn().getResponse().getContentAsString());
            var reportId = findReportIdByTargetId(pendingReports, noteId);

            mvc.perform(patch("/api/v1/admin/reports/" + reportId)
                            .header("Authorization", adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"status":"APPROVED","adminNote":"Step13 note hidden by moderation"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.adminNote").value("Step13 note hidden by moderation"));

            mvc.perform(get("/api/v1/community/share/" + noteToken))
                    .andExpect(status().isNotFound());

            var notifications = KafkaTestUtils.getRecords(notificationConsumer, Duration.ofSeconds(10));
            var values = StreamSupport.stream(notifications.spliterator(), false)
                    .map(record -> (NotificationSend) record.value())
                    .toList();

            assertThat(values).anySatisfy(notification -> {
                assertThat(notification.getTenantId()).isEqualTo("default");
                assertThat(notification.getUserId()).isEqualTo("13101");
                assertThat(notification.getNotificationType()).isEqualTo("REPORT_RESOLVED");
                assertThat(notification.getData()).containsEntry("targetType", "SHARED_NOTE");
            });
            assertThat(values).anySatisfy(notification -> {
                assertThat(notification.getTenantId()).isEqualTo("default");
                assertThat(notification.getUserId()).isEqualTo("13100");
                assertThat(notification.getNotificationType()).isEqualTo("CONTENT_REMOVED");
                assertThat(notification.getData()).containsEntry("reportId", String.valueOf(reportId));
            });
        }
    }

    private org.apache.kafka.clients.consumer.Consumer<String, Object> notificationConsumer() {
        var consumerProps = KafkaTestUtils.consumerProps("step13-notification-contract", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://community-step13");
        consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new KafkaAvroDeserializer()
        );
        return consumerFactory.createConsumer();
    }

    private JsonNode json(String body) throws Exception {
        return objectMapper.readTree(body);
    }

    private long findReportIdByTargetId(JsonNode reports, long targetId) {
        for (JsonNode report : reports) {
            if (report.get("targetId").asLong() == targetId) {
                return report.get("id").asLong();
            }
        }
        throw new AssertionError("Pending report not found for targetId=" + targetId);
    }

    private String bearer(String subject) {
        return "Bearer " + TestJwt.accessToken(subject);
    }

    private String bearer(String subject, List<String> roles) {
        return "Bearer " + TestJwt.accessToken(subject, roles);
    }
}
