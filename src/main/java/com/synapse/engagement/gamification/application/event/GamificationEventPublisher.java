package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.gamification.api.dto.BadgeResponse;

public interface GamificationEventPublisher {
    /**
     * @param userId         내부 PK(Long) — dedupe eventId 도출 등 내부 식별용으로만 사용.
     * @param externalUserId platform UUID 문자열 — outbound 이벤트 userId에 그대로 실린다(F10).
     *                       platform NotificationService가 UUID.fromString(userId) 하므로 반드시 UUID여야 한다.
     */
    void publishLevelUp(Long userId, String externalUserId, String tenantId, int oldLevel, int newLevel, int totalXp);

    void publishBadgeEarned(Long userId, String externalUserId, String tenantId, BadgeResponse badge);
}
