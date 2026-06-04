package com.synapse.engagement.community.application.event;

public interface CommunityNotificationPublisher {
    /** 모더레이션 결과 알림 1건 발행. recipientUserId/tenantId/유형/제목/본문. */
    void publishModerationNotification(
            Long recipientUserId, String tenantId, String notificationType,
            String title, String body, java.util.Map<String, String> data);
}
