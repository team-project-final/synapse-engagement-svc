package com.synapse.engagement.gamification.dto.response;

import com.synapse.engagement.gamification.entity.EventType;
import java.time.LocalDateTime;
import java.util.UUID;

/*
 * GET /api/v1/gamification/xp/history 응답 DTO입니다.
 * 한 줄이 "언제, 어떤 활동으로, 몇 XP를 받았는지"를 의미합니다.
 */
public record XpEventResponse(
        // XP 이벤트 로그의 ID입니다.
        UUID id,
        // XP가 발생한 활동 종류입니다.
        EventType eventType,
        // 이 활동으로 지급된 XP입니다.
        int xpAmount,
        // XP 원본 대상 ID입니다. 예: 카드 ID, 노트 ID.
        String sourceId,
        // 원본 대상 종류입니다. 예: CARD, NOTE.
        String sourceType,
        // XP가 적립된 시각입니다.
        LocalDateTime createdAt) {
}

