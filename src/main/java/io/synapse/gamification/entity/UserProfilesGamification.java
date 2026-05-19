package io.synapse.gamification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_profiles_gamification")
public class UserProfilesGamification {

    private static final int XP_PER_LEVEL = 100;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "total_xp", nullable = false)
    private int totalXp;

    @Column(nullable = false)
    private int level;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserProfilesGamification() {
    }

    private UserProfilesGamification(UUID userId) {
        this.userId = Objects.requireNonNull(userId);
        this.totalXp = 0;
        this.level = 1;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.title = "Novice";
        this.updatedAt = LocalDateTime.now();
    }

    public static UserProfilesGamification create(UUID userId) {
        return new UserProfilesGamification(userId);
    }

    public void addXp(int xpAmount) {
        if (xpAmount <= 0) {
            throw new IllegalArgumentException("XP amount must be positive.");
        }

        totalXp += xpAmount;
        level = calculateLevel(totalXp);
        title = titleFor(level);
        updatedAt = LocalDateTime.now();
    }

    public int nextLevelXp() {
        return level * XP_PER_LEVEL;
    }

    public UUID userId() {
        return userId;
    }

    public int totalXp() {
        return totalXp;
    }

    public int level() {
        return level;
    }

    public int currentStreak() {
        return currentStreak;
    }

    public int longestStreak() {
        return longestStreak;
    }

    public String title() {
        return title;
    }

    private static int calculateLevel(int totalXp) {
        return (totalXp / XP_PER_LEVEL) + 1;
    }

    private static String titleFor(int level) {
        if (level >= 10) {
            return "Master";
        }
        if (level >= 5) {
            return "Scholar";
        }
        return "Novice";
    }
}
