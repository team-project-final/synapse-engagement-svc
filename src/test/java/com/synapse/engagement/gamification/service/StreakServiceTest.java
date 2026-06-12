package com.synapse.engagement.gamification.service;

import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StreakServiceTest {

    private UserProfilesGamification profile;

    @BeforeEach
    void setUp() {
        profile = UserProfilesGamification.create(UUID.randomUUID());
    }

    @Test
    @DisplayName("updateStreak_최초활동_streak1시작")
    void updateStreak_최초활동_streak1시작() {
        profile.updateStreak(LocalDate.of(2026, 6, 1));

        assertThat(profile.currentStreak()).isEqualTo(1);
        assertThat(profile.longestStreak()).isEqualTo(1);
        assertThat(profile.lastActivityDate()).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    @Test
    @DisplayName("updateStreak_연속이틀_streak2")
    void updateStreak_연속이틀_streak2() {
        profile.updateStreak(LocalDate.of(2026, 6, 1));
        profile.updateStreak(LocalDate.of(2026, 6, 2));

        assertThat(profile.currentStreak()).isEqualTo(2);
        assertThat(profile.longestStreak()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateStreak_하루건너뜀_streak리셋")
    void updateStreak_하루건너뜀_streak리셋() {
        profile.updateStreak(LocalDate.of(2026, 6, 1));
        profile.updateStreak(LocalDate.of(2026, 6, 2));
        profile.updateStreak(LocalDate.of(2026, 6, 4)); // gap

        assertThat(profile.currentStreak()).isEqualTo(1);
        assertThat(profile.longestStreak()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateStreak_같은날두번_streak유지")
    void updateStreak_같은날두번_streak유지() {
        profile.updateStreak(LocalDate.of(2026, 6, 1));
        profile.updateStreak(LocalDate.of(2026, 6, 1));

        assertThat(profile.currentStreak()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateStreak_7일연속_longestStreak7")
    void updateStreak_7일연속_longestStreak7() {
        for (int i = 1; i <= 7; i++) {
            profile.updateStreak(LocalDate.of(2026, 6, i));
        }

        assertThat(profile.currentStreak()).isEqualTo(7);
        assertThat(profile.longestStreak()).isEqualTo(7);
    }
}
