package com.synapse.engagement.gamification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_badges")
public class UserBadge {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_code", referencedColumnName = "code", nullable = false)
    private Badge badge;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    protected UserBadge() {
    }

    private UserBadge(UUID userId, Badge badge) {
        this.id = UUID.randomUUID();
        this.userId = Objects.requireNonNull(userId);
        this.badge = Objects.requireNonNull(badge);
        this.earnedAt = LocalDateTime.now();
    }

    public static UserBadge award(UUID userId, Badge badge) {
        return new UserBadge(userId, badge);
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public Badge badge() {
        return badge;
    }

    public LocalDateTime earnedAt() {
        return earnedAt;
    }
}
