package com.synapse.engagement.gamification.application;

import com.synapse.engagement.gamification.api.dto.AddXpRequest;
import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import com.synapse.engagement.gamification.application.event.GamificationEventPublisher;
import com.synapse.engagement.gamification.domain.BadgeConditionType;
import com.synapse.engagement.gamification.domain.EventType;
import com.synapse.engagement.gamification.domain.UserProfilesGamification;
import com.synapse.engagement.gamification.domain.UserStreak;
import com.synapse.engagement.gamification.repository.UserProfilesGamificationRepository;
import com.synapse.engagement.gamification.repository.XpEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GamificationStep7EventServiceTests {
    @Test
    void addXpPublishesLevelUpAndBadgeEarnedEvents() {
        var xpEventRepository = mock(XpEventRepository.class);
        var profileRepository = mock(UserProfilesGamificationRepository.class);
        var badgeService = mock(BadgeService.class);
        var streakService = mock(StreakService.class);
        var eventPublisher = mock(GamificationEventPublisher.class);
        var service = new GamificationService(
                xpEventRepository,
                profileRepository,
                badgeService,
                new LevelService(),
                streakService,
                eventPublisher
        );
        var profile = UserProfilesGamification.initialize(70L);
        profile.addXp(90, 1);
        var streak = UserStreak.initialize(70L);
        var badge = new BadgeResponse(
                "LEVEL_2",
                "Level 2",
                "Reach level 2",
                null,
                BadgeConditionType.LEVEL,
                2,
                Instant.now()
        );
        var request = new AddXpRequest(EventType.CARD_REVIEWED, 20, "card-70", "card", "event-70");

        when(xpEventRepository.existsByEventId("event-70")).thenReturn(false);
        when(xpEventRepository.existsByUserIdAndEventTypeAndSourceId(70L, EventType.CARD_REVIEWED, "card-70"))
                .thenReturn(false);
        when(profileRepository.findById(70L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserProfilesGamification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(streakService.recordActivity(70L)).thenReturn(streak);
        when(badgeService.awardEligibleBadges(eq(70L), any(UserProfilesGamification.class), eq(streak)))
                .thenReturn(List.of(badge));

        // externalUserId(platform UUID)를 그대로 전파한다(F10).
        var externalUserId = "55555555-5555-5555-5555-555555555555";
        service.addXp(70L, externalUserId, "tenant-a", request);

        verify(eventPublisher).publishLevelUp(70L, externalUserId, "tenant-a", 1, 2, 110);
        verify(eventPublisher).publishBadgeEarned(70L, externalUserId, "tenant-a", badge);
    }
}
