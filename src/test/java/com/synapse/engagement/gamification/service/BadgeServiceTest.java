package com.synapse.engagement.gamification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.engagement.gamification.entity.Badge;
import com.synapse.engagement.gamification.entity.UserBadge;
import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import com.synapse.engagement.gamification.repository.BadgeRepository;
import com.synapse.engagement.gamification.repository.UserBadgeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepo;

    @Mock
    private UserBadgeRepository userBadgeRepo;

    private BadgeService badgeService;

    @BeforeEach
    void setUp() {
        badgeService = new BadgeService(badgeRepo, userBadgeRepo, new ObjectMapper());
    }

    @Test
    @DisplayName("evaluateAndAward_XP조건달성_배지수여")
    void evaluateAndAward_XP조건달성_배지수여() {
        UUID userId = UUID.randomUUID();
        UserProfilesGamification profile = UserProfilesGamification.create(userId);
        profile.addXp(100);

        Badge badge = badgeMock("CENTURION", "{\"type\":\"xp_threshold\",\"value\":100}");
        given(badgeRepo.findAll()).willReturn(List.of(badge));
        given(userBadgeRepo.findBadgeCodesByUserId(userId)).willReturn(List.of());
        given(userBadgeRepo.save(any(UserBadge.class))).willAnswer(inv -> inv.getArgument(0));

        List<UserBadge> awarded = badgeService.evaluateAndAward(profile);

        assertThat(awarded).hasSize(1);
        assertThat(awarded.get(0).badge().code()).isEqualTo("CENTURION");
    }

    @Test
    @DisplayName("evaluateAndAward_XP미달_배지미수여")
    void evaluateAndAward_XP미달_배지미수여() {
        UUID userId = UUID.randomUUID();
        UserProfilesGamification profile = UserProfilesGamification.create(userId);
        profile.addXp(50);

        Badge badge = badgeMock("CENTURION", "{\"type\":\"xp_threshold\",\"value\":100}");
        given(badgeRepo.findAll()).willReturn(List.of(badge));
        given(userBadgeRepo.findBadgeCodesByUserId(userId)).willReturn(List.of());

        List<UserBadge> awarded = badgeService.evaluateAndAward(profile);

        assertThat(awarded).isEmpty();
        verify(userBadgeRepo, never()).save(any());
    }

    @Test
    @DisplayName("evaluateAndAward_이미획득한배지_중복수여방지")
    void evaluateAndAward_이미획득한배지_중복수여방지() {
        UUID userId = UUID.randomUUID();
        UserProfilesGamification profile = UserProfilesGamification.create(userId);
        profile.addXp(100);

        Badge badge = badgeMock("CENTURION", "{\"type\":\"xp_threshold\",\"value\":100}");
        given(badgeRepo.findAll()).willReturn(List.of(badge));
        // 이미 획득한 배지 → criteriaJson은 호출되지 않습니다.
        given(userBadgeRepo.findBadgeCodesByUserId(userId)).willReturn(List.of("CENTURION"));

        List<UserBadge> awarded = badgeService.evaluateAndAward(profile);

        assertThat(awarded).isEmpty();
        verify(userBadgeRepo, never()).save(any());
    }

    @Test
    @DisplayName("evaluateAndAward_스트릭7일달성_배지수여")
    void evaluateAndAward_스트릭7일달성_배지수여() {
        UUID userId = UUID.randomUUID();
        UserProfilesGamification profile = UserProfilesGamification.create(userId);
        for (int i = 1; i <= 7; i++) {
            profile.updateStreak(LocalDate.of(2026, 6, i));
        }

        Badge badge = badgeMock("STREAK_7", "{\"type\":\"streak_threshold\",\"value\":7}");
        given(badgeRepo.findAll()).willReturn(List.of(badge));
        given(userBadgeRepo.findBadgeCodesByUserId(userId)).willReturn(List.of());
        given(userBadgeRepo.save(any(UserBadge.class))).willAnswer(inv -> inv.getArgument(0));

        List<UserBadge> awarded = badgeService.evaluateAndAward(profile);

        assertThat(awarded).hasSize(1);
        assertThat(awarded.get(0).badge().code()).isEqualTo("STREAK_7");
    }

    // criteriaJson: 알려진 배지가 아닌 경우 호출 안 될 수 있으므로 lenient 사용
    private Badge badgeMock(String code, String criteriaJson) {
        Badge b = mock(Badge.class);
        given(b.code()).willReturn(code);
        lenient().when(b.criteriaJson()).thenReturn(criteriaJson);
        return b;
    }
}
