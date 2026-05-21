package com.synapse.engagement.gamification.entity;

/*
 * XP가 어떤 종류의 활동에서 발생했는지 표현합니다.
 * 문자열을 코드 곳곳에 직접 쓰지 않고 enum으로 모아두면 오타와 잘못된 값을 줄일 수 있습니다.
 */
public enum EventType {
    // 사용자가 카드를 복습 완료했을 때 지급되는 XP입니다.
    CARD_REVIEWED,
    // 사용자가 노트를 만들었을 때 지급되는 XP입니다.
    NOTE_CREATED
}

