package com.synapse.engagement.gamification.service;

import com.synapse.engagement.gamification.entity.EventType;
import java.util.UUID;

/*
 * XP 적립 요청을 서비스 계층으로 넘길 때 사용하는 값 객체입니다.
 *
 * Controller 요청 DTO가 아니라 내부 유스케이스용 Command로 둔 이유:
 * 후속 단계에서 다른 입력 경로가 생겨도 같은 addXp 로직을 재사용할 수 있게 하기 위해서입니다.
 */
public record AddXpCommand(
        // XP를 받을 사용자입니다. 현재는 platform-svc 사용자 ID를 UUID로 논리 참조합니다.
        UUID userId,
        // 어떤 활동으로 XP가 발생했는지 구분합니다. 예: CARD_REVIEWED, NOTE_CREATED.
        EventType eventType,
        // 이번 활동으로 지급할 XP입니다. Step 4 기본 규칙은 학습 활동 1회 = 10 XP입니다.
        int xpAmount,
        // XP가 발생한 원본 대상의 ID입니다. 예: card-1, note-3.
        String sourceId,
        // sourceId가 어떤 종류의 원본인지 알려줍니다. 예: CARD, NOTE.
        String sourceType,
        // 이벤트 자체의 고유 ID입니다. 재시도/중복 요청을 막는 멱등성 키로 사용합니다.
        String eventId) {
}

