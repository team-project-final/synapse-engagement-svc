package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.gamification.api.dto.AddXpRequest;
import com.synapse.engagement.gamification.application.GamificationService;
import com.synapse.engagement.gamification.domain.EventType;
import com.synapse.engagement.gamification.domain.UserProfilesGamification;
import com.synapse.engagement.gamification.repository.UserProfilesGamificationRepository;
import com.synapse.engagement.shared.ConflictException;
import com.synapse.learning.Rating;
import com.synapse.learning.ReviewCompleted;
import com.synapse.platform.UserRegistered;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EngagementKafkaEventHandlerTests {
    private final UserProfilesGamificationRepository profileRepository = mock(UserProfilesGamificationRepository.class);
    private final GamificationService gamificationService = mock(GamificationService.class);
    private final EngagementKafkaEventHandler handler = new EngagementKafkaEventHandler(
            profileRepository,
            gamificationService
    );

    @Test
    void userRegisteredCreatesGamificationProfileWhenMissing() {
        when(profileRepository.existsById(700L)).thenReturn(false);

        handler.handleUserRegistered(new UserRegistered(
                "700",
                "user700@example.com",
                "tenant-a",
                "2026-06-02T00:00:00Z"
        ));

        var profileCaptor = ArgumentCaptor.forClass(UserProfilesGamification.class);
        verify(profileRepository).save(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getUserId()).isEqualTo(700L);
        assertThat(profileCaptor.getValue().getTotalXp()).isZero();
        assertThat(profileCaptor.getValue().getLevel()).isEqualTo(1);
    }

    @Test
    void userRegisteredIsIdempotentWhenProfileAlreadyExists() {
        when(profileRepository.existsById(700L)).thenReturn(true);

        handler.handleUserRegistered(new UserRegistered(
                "700",
                "user700@example.com",
                "tenant-a",
                "2026-06-02T00:00:00Z"
        ));

        verify(profileRepository, never()).save(any());
    }

    @Test
    void reviewCompletedAddsCardReviewedXpWithDerivedIdempotencyKey() {
        var event = new ReviewCompleted(
                "card-1",
                "800",
                "tenant-learning",
                Rating.GOOD,
                "2026-06-03T00:00:00Z",
                "2026-06-02T00:00:00Z"
        );

        handler.handleReviewCompleted(event);

        var requestCaptor = ArgumentCaptor.forClass(AddXpRequest.class);
        verify(gamificationService).addXp(eq(800L), eq("tenant-learning"), requestCaptor.capture());
        var request = requestCaptor.getValue();
        assertThat(request.eventType()).isEqualTo(EventType.CARD_REVIEWED);
        assertThat(request.xpAmount()).isNull();
        assertThat(request.sourceType()).isEqualTo("card-review");
        assertThat(request.sourceId()).isEqualTo("review-completed:card-1:2026-06-02T00:00:00Z");
        assertThat(request.eventId()).isEqualTo("review-completed:card-1:2026-06-02T00:00:00Z");
    }

    @Test
    void reviewCompletedSkipsDuplicateXpEventWithoutCrashingConsumer() {
        var event = new ReviewCompleted(
                "card-1",
                "800",
                "tenant-learning",
                Rating.GOOD,
                "2026-06-03T00:00:00Z",
                "2026-06-02T00:00:00Z"
        );
        when(gamificationService.addXp(eq(800L), eq("tenant-learning"), any()))
                .thenThrow(new ConflictException("XP event already processed"));

        assertThatCode(() -> handler.handleReviewCompleted(event)).doesNotThrowAnyException();
    }
}
