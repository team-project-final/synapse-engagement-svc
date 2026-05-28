package com.synapse.engagement.gamification.application;

import com.synapse.engagement.gamification.application.event.GamificationEventPublisher;
import com.synapse.engagement.gamification.domain.Badge;
import com.synapse.engagement.gamification.domain.BadgeConditionType;
import com.synapse.engagement.gamification.domain.EventType;
import com.synapse.engagement.gamification.domain.UserProfilesGamification;
import com.synapse.engagement.gamification.domain.UserStreak;
import com.synapse.engagement.gamification.repository.XpEventRepository;
import com.synapse.engagement.gamification.repository.BadgeRepository;
import com.synapse.engagement.gamification.repository.UserBadgeRepository;
import com.synapse.engagement.gamification.repository.UserProfilesGamificationRepository;
import com.synapse.engagement.gamification.repository.UserStreakRepository;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class GamificationStep6ServiceTests {
    @Test
    void levelServiceCalculatesXpThresholds() {
        var levelService = new LevelService();

        assertThat(levelService.calculateLevel(99)).isEqualTo(1);
        assertThat(levelService.calculateLevel(100)).isEqualTo(2);
        assertThat(levelService.calculateLevel(300)).isEqualTo(3);
        assertThat(levelService.calculateLevel(600)).isEqualTo(4);
    }

    @Test
    void streakServiceCreatesAndRecordsFirstActivity() {
        var repository = mock(UserStreakRepository.class);
        var service = new StreakService(repository);

        when(repository.findByUserId(10L)).thenReturn(Optional.empty());
        when(repository.save(any(UserStreak.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var streak = service.recordActivity(10L);

        assertThat(streak.getUserId()).isEqualTo(10L);
        assertThat(streak.getCurrentStreak()).isEqualTo(1);
        assertThat(streak.getLongestStreak()).isEqualTo(1);
    }

    @Test
    void badgeServiceAwardsConditionMatchedBadgesOnlyOnce() {
        var badgeRepository = mock(BadgeRepository.class);
        var userBadgeRepository = mock(UserBadgeRepository.class);
        var service = new BadgeService(badgeRepository, userBadgeRepository);
        var profile = UserProfilesGamification.initialize(20L);
        profile.addXp(150, 2);
        var streak = UserStreak.initialize(20L);
        streak.recordActivity(java.time.LocalDate.now());

        var firstXp = Badge.create(
                "FIRST_XP",
                "First XP",
                "Earn XP for the first time",
                null,
                BadgeConditionType.TOTAL_XP,
                1
        );
        var streakThree = Badge.create(
                "STREAK_3",
                "3 Day Streak",
                "Keep a 3 day activity streak",
                null,
                BadgeConditionType.STREAK,
                3
        );

        when(badgeRepository.existsByCode(any())).thenReturn(true);
        when(badgeRepository.findAll()).thenReturn(List.of(firstXp, streakThree));
        when(userBadgeRepository.existsByUserIdAndBadgeCode(20L, "FIRST_XP")).thenReturn(false);
        when(userBadgeRepository.existsByUserIdAndBadgeCode(20L, "STREAK_3")).thenReturn(false);
        when(userBadgeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var awarded = service.awardEligibleBadges(20L, profile, streak);

        assertThat(awarded).extracting("code").containsExactly("FIRST_XP");
    }

    @Test
    void leaderboardServiceRanksByRepositoryOrderAndClampsLimit() {
        var repository = mock(UserProfilesGamificationRepository.class);
        var service = new LeaderboardService(repository);
        var top = UserProfilesGamification.initialize(30L);
        top.addXp(500, 3);
        var second = UserProfilesGamification.initialize(40L);
        second.addXp(100, 2);

        when(repository.findByOrderByTotalXpDescUserIdAsc(PageRequest.of(0, 100)))
                .thenReturn(List.of(top, second));

        var leaderboard = service.findLeaderboard(500);

        assertThat(leaderboard).extracting("rank").containsExactly(1, 2);
        assertThat(leaderboard).extracting("userId").containsExactly(30L, 40L);
        verify(repository).findByOrderByTotalXpDescUserIdAsc(PageRequest.of(0, 100));
    }

    @Test
    void gamificationServiceRejectsDuplicateXpEventBeforeMutatingProfile() {
        var xpEventRepository = mock(XpEventRepository.class);
        var profileRepository = mock(UserProfilesGamificationRepository.class);
        var badgeService = mock(BadgeService.class);
        var levelService = new LevelService();
        var streakService = mock(StreakService.class);
        var eventPublisher = mock(GamificationEventPublisher.class);
        var service = new GamificationService(
                xpEventRepository,
                profileRepository,
                badgeService,
                levelService,
                streakService,
                eventPublisher
        );
        var request = new com.synapse.engagement.gamification.api.dto.AddXpRequest(
                EventType.CARD_REVIEWED,
                10,
                "card-1",
                "card",
                "event-1"
        );

        when(xpEventRepository.existsByEventId("event-1")).thenReturn(true);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.addXp(50L, request))
                .isInstanceOf(com.synapse.engagement.shared.ConflictException.class)
                .hasMessageContaining("already processed");
        verify(profileRepository, never()).save(any());
        verify(streakService, never()).recordActivity(any());
        verify(eventPublisher, never()).publishLevelUp(any(), any(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void gamificationServiceAddsXpRecordsStreakAndAwardsBadges() {
        var xpEventRepository = mock(XpEventRepository.class);
        var profileRepository = mock(UserProfilesGamificationRepository.class);
        var badgeService = mock(BadgeService.class);
        var levelService = new LevelService();
        var streakService = mock(StreakService.class);
        var eventPublisher = mock(GamificationEventPublisher.class);
        var service = new GamificationService(
                xpEventRepository,
                profileRepository,
                badgeService,
                levelService,
                streakService,
                eventPublisher
        );
        var request = new com.synapse.engagement.gamification.api.dto.AddXpRequest(
                EventType.NOTE_CREATED,
                150,
                "note-1",
                "note",
                "event-2"
        );
        var streak = UserStreak.initialize(60L);
        streak.recordActivity(java.time.LocalDate.now());

        when(xpEventRepository.existsByEventId("event-2")).thenReturn(false);
        when(xpEventRepository.existsByUserIdAndEventTypeAndSourceId(60L, EventType.NOTE_CREATED, "note-1"))
                .thenReturn(false);
        when(profileRepository.findById(60L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfilesGamification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(streakService.recordActivity(60L)).thenReturn(streak);
        when(badgeService.awardEligibleBadges(eq(60L), any(UserProfilesGamification.class), eq(streak)))
                .thenReturn(List.of());

        var response = service.addXp(60L, request);

        assertThat(response.xp()).isEqualTo(150);
        assertThat(response.level()).isEqualTo(2);
        assertThat(response.currentStreak()).isEqualTo(1);
        verify(xpEventRepository).save(any());
        verify(badgeService).awardEligibleBadges(eq(60L), any(UserProfilesGamification.class), eq(streak));
        verify(eventPublisher).publishLevelUp(60L, "default", 1, 2, 150);
    }
}
