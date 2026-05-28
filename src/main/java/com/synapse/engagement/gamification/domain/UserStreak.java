package com.synapse.engagement.gamification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "user_streaks")
public class UserStreak {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserStreak() {
    }

    private UserStreak(Long userId) {
        this.userId = userId;
    }

    public static UserStreak initialize(Long userId) {
        return new UserStreak(userId);
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public void recordActivity(LocalDate today) {
        if (lastActivityDate == null) {
            currentStreak = 1;
        } else if (lastActivityDate.plusDays(1).equals(today)) {
            currentStreak += 1;
        } else if (!lastActivityDate.equals(today)) {
            currentStreak = 1;
        }
        longestStreak = Math.max(longestStreak, currentStreak);
        lastActivityDate = today;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }
}
