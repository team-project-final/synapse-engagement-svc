package io.synapse.gamification.entity;

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
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return normalized;
    }
}
