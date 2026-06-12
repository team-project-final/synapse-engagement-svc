package com.synapse.engagement.community.application.event;

import com.synapse.platform.NotificationSend;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "synapse.kafka", name = "enabled", havingValue = "true")
public class CommunityNotificationKafkaProducer implements CommunityNotificationPublisher {
    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;
    private final String notificationSendTopic;
    private final Clock clock = Clock.systemUTC();

    public CommunityNotificationKafkaProducer(
            KafkaTemplate<String, SpecificRecord> kafkaTemplate,
            @Value("${synapse.kafka.topics.notification-send}") String notificationSendTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationSendTopic = notificationSendTopic;
    }

    @Override
    public void publishModerationNotification(Long recipientUserId, String tenantId, String notificationType,
                                             String title, String body, Map<String, String> data) {
        // 필드는 platform-canonical NotificationSend.avsc와 일치(eventId/occurredAt/traceparent 필수). userId는 gamification과 동일하게 Long 문자열화.
        var event = NotificationSend.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTenantId(tenantId)
                .setOccurredAt(clock.millis())
                .setTraceparent(null)
                .setUserId(String.valueOf(recipientUserId))
                .setNotificationType(notificationType)
                .setChannels(List.of("FCM"))
                .setTitle(title)
                .setBody(body)
                .setData(data)
                .build();
        // EVENT_CONTRACT_STANDARD: tenant 순서 보장을 위해 tenantId를 partition key로.
        kafkaTemplate.send(notificationSendTopic, tenantId, event);
    }
}
