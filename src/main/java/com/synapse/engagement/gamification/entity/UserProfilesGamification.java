package com.synapse.engagement.gamification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_profiles_gamification")
public class UserProfilesGamification {

    /*
     * Step 4에서는 간단한 레벨 규칙을 사용합니다.
     * 총 XP가 0~99이면 Lv1, 100~199이면 Lv2처럼 100 XP마다 레벨이 1씩 오릅니다.
     * W3 Step 6에서는 level_definitions 테이블 기반으로 확장될 예정입니다.
     */
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

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserProfilesGamification() {
    }

    private UserProfilesGamification(UUID userId) {
        // 신규 사용자는 XP가 없어도 기본 프로필을 가질 수 있도록 Lv1/Novice로 시작합니다.
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
        // 이 엔티티는 "적립"만 담당합니다. XP 차감/소모는 Step 4 범위 밖입니다.
        if (xpAmount <= 0) {
            throw new IllegalArgumentException("XP amount must be positive.");
        }

        /*
         * totalXp는 누적값이고 level/title은 totalXp에서 다시 계산되는 파생 상태입니다.
         * XP를 더한 직후 함께 갱신해야 응답과 DB 상태가 서로 어긋나지 않습니다.
         */
        totalXp += xpAmount;
        level = calculateLevel(totalXp);
        title = titleFor(level);
        updatedAt = LocalDateTime.now();
    }

    public void applyLevel(int newLevel, String newTitle) {
        // LevelService가 level_definitions 기반으로 계산한 레벨/칭호를 적용합니다.
        this.level = newLevel;
        this.title = newTitle;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStreak(LocalDate today) {
        // KST 자정 기준으로 연속 학습일을 계산합니다.
        // 같은 날 여러 번 XP를 받아도 스트릭은 1회만 증가합니다.
        if (lastActivityDate == null || today.isAfter(lastActivityDate.plusDays(1))) {
            currentStreak = 1;
        } else if (today.equals(lastActivityDate.plusDays(1))) {
            currentStreak++;
        }
        longestStreak = Math.max(longestStreak, currentStreak);
        lastActivityDate = today;
        updatedAt = LocalDateTime.now();
    }

    public LocalDate lastActivityDate() {
        return lastActivityDate;
    }

    public int nextLevelXp() {
        // LevelService 미연동 시 fallback용 단순 계산입니다.
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
        // 정수 나눗셈을 이용해 100 XP 단위로 레벨 구간을 나눕니다.
        return (totalXp / XP_PER_LEVEL) + 1;
    }

    private static String titleFor(int level) {
        // Step 4에서는 간단한 칭호만 제공하고, 배지/칭호 고도화는 Step 6에서 확장합니다.
        if (level >= 10) {
            return "Master";
        }
        if (level >= 5) {
            return "Scholar";
        }
        return "Novice";
    }
}

