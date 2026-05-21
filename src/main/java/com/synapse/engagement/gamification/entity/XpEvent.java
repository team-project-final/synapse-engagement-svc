package com.synapse.engagement.gamification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "xp_events")
public class XpEvent {

    /*
     * xp_events는 "XP 적립 로그" 테이블입니다.
     * user_profiles_gamification이 현재 누적 상태를 빠르게 보여준다면,
     * 이 엔티티는 어떤 활동들이 XP를 만들었는지 감사 로그처럼 남깁니다.
     */
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "xp_amount", nullable = false)
    private int xpAmount;

    @Column(name = "source_id", nullable = false, length = 100)
    private String sourceId;

    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected XpEvent() {
    }

    private XpEvent(UUID userId, EventType eventType, int xpAmount, String sourceId, String sourceType, String eventId) {
        this.id = UUID.randomUUID();
        this.userId = Objects.requireNonNull(userId);
        this.eventType = Objects.requireNonNull(eventType);
        this.xpAmount = xpAmount;
        this.sourceId = requireText(sourceId, "sourceId");
        this.sourceType = requireText(sourceType, "sourceType");
        this.eventId = requireText(eventId, "eventId");
        this.createdAt = LocalDateTime.now();
    }

    public static XpEvent create(
            UUID userId,
            EventType eventType,
            int xpAmount,
            String sourceId,
            String sourceType,
            String eventId) {
        // XP 이벤트는 적립만 기록하므로 0 이하의 XP는 잘못된 입력으로 봅니다.
        if (xpAmount <= 0) {
            throw new IllegalArgumentException("XP amount must be positive.");
        }
        return new XpEvent(userId, eventType, xpAmount, sourceId, sourceType, eventId);
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public EventType eventType() {
        return eventType;
    }

    public int xpAmount() {
        return xpAmount;
    }

    public String sourceId() {
        return sourceId;
    }

    public String sourceType() {
        return sourceType;
    }

    public String eventId() {
        return eventId;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    private static String requireText(String value, String fieldName) {
        // sourceId/sourceType/eventId는 DB 멱등성 기준이므로 빈 문자열로 저장되면 안 됩니다.
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return normalized;
    }
}

