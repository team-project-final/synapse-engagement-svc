package com.synapse.engagement.community.application.event;

import com.synapse.platform.NotificationSend;
import com.synapse.engagement.shared.KafkaTopicResolver;
import org.apache.avro.specific.SpecificRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CommunityNotificationKafkaProducerTests {
    @Test
    void publishesNotificationSendWithTenantKey() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, SpecificRecord> template = mock(KafkaTemplate.class);
        var producer = new CommunityNotificationKafkaProducer(
                template,
                new KafkaTopicResolver(
                        "",
                        "engagement.gamification.level-up-v1",
                        "engagement.gamification.badge-earned-v1",
                        "platform.auth.user-registered-v1",
                        "learning.card.review-completed-v1",
                        "platform.notification.notification-send-v1"
                )
        );

        producer.publishModerationNotification(42L, "tenant-1", "REPORT_RESOLVED",
                "신고가 처리되었습니다", "본문", Map.of("reportId", "7"));

        var captor = org.mockito.ArgumentCaptor.forClass(SpecificRecord.class);
        verify(template).send(eq("platform.notification.notification-send-v1"), eq("tenant-1"), captor.capture());
        NotificationSend sent = (NotificationSend) captor.getValue();
        assertThat(sent.getUserId()).isEqualTo("42");
        assertThat(sent.getNotificationType()).isEqualTo("REPORT_RESOLVED");
        assertThat(sent.getChannels()).contains("FCM");
        assertThat(sent.getEventId()).isNotNull();
        assertThat(sent.getOccurredAt()).isPositive();
    }
}
