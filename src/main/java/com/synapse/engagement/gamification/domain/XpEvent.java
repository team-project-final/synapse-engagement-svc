package com.synapse.engagement.gamification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "xp_events")
public class XpEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

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
    private Instant createdAt;

    protected XpEvent() {
    }

    private XpEvent(Long userId, EventType eventType, int xpAmount, String sourceId, String sourceType, String eventId) {
        this.userId = userId;
        this.eventType = eventType;
        this.xpAmount = xpAmount;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.eventId = eventId;
    }

    public static XpEvent create(Long userId, EventType eventType, int xpAmount, String sourceId, String sourceType, String eventId) {
        return new XpEvent(userId, eventType, xpAmount, sourceId, sourceType, eventId);
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public int getXpAmount() {
        return xpAmount;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
