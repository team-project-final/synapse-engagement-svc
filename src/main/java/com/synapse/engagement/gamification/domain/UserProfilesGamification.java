package com.synapse.engagement.gamification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_profiles_gamification")
public class UserProfilesGamification {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_xp", nullable = false)
    private int totalXp;

    @Column(nullable = false)
    private int level;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserProfilesGamification() {
    }

    private UserProfilesGamification(Long userId) {
        this.userId = userId;
        this.level = 1;
    }

    public static UserProfilesGamification initialize(Long userId) {
        return new UserProfilesGamification(userId);
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public void addXp(int amount, int level) {
        this.totalXp += amount;
        this.level = level;
    }

    public static int calculateLevel(int totalXp) {
        if (totalXp < 100) {
            return 1;
        }
        if (totalXp < 300) {
            return 2;
        }
        if (totalXp < 600) {
            return 3;
        }
        return 4 + ((totalXp - 600) / 500);
    }

    public static int nextLevelXp(int level) {
        return switch (level) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 600;
            default -> 600 + ((level - 3) * 500);
        };
    }

    public Long getUserId() {
        return userId;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public int getLevel() {
        return level;
    }

}
