package com.synapse.engagement.gamification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_badges")
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "earned_at", nullable = false)
    private Instant earnedAt;

    protected UserBadge() {
    }

    private UserBadge(Long userId, Badge badge) {
        this.userId = userId;
        this.badge = badge;
    }

    public static UserBadge earn(Long userId, Badge badge) {
        return new UserBadge(userId, badge);
    }

    @PrePersist
    void onCreate() {
        earnedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Badge getBadge() {
        return badge;
    }

    public Instant getEarnedAt() {
        return earnedAt;
    }
}
