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

        handler.handleUserRegistered(UserRegistered.newBuilder()
                .setEventId("evt-700")
                .setTenantId("tenant-a")
                .setOccurredAt(1748822400000L)
                .setUserId("700")
                .setEmail("user700@example.com")
                .setDisplayName("User 700")
                .build());

        var profileCaptor = ArgumentCaptor.forClass(UserProfilesGamification.class);
        verify(profileRepository).save(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getUserId()).isEqualTo(700L);
        assertThat(profileCaptor.getValue().getTotalXp()).isZero();
        assertThat(profileCaptor.getValue().getLevel()).isEqualTo(1);
    }

    @Test
    void userRegisteredIsIdempotentWhenProfileAlreadyExists() {
        when(profileRepository.existsById(700L)).thenReturn(true);

        handler.handleUserRegistered(UserRegistered.newBuilder()
                .setEventId("evt-700")
                .setTenantId("tenant-a")
                .setOccurredAt(1748822400000L)
                .setUserId("700")
                .setEmail("user700@example.com")
                .setDisplayName("User 700")
                .build());

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
        // 원본 UUID(여기선 "800")를 externalUserId로 그대로 전달한다(F10), 내부 PK는 Long(800L).
        verify(gamificationService).addXp(eq(800L), eq("800"), eq("tenant-learning"), requestCaptor.capture());
        var request = requestCaptor.getValue();
        assertThat(request.eventType()).isEqualTo(EventType.CARD_REVIEWED);
        assertThat(request.xpAmount()).isNull();
        assertThat(request.sourceType()).isEqualTo("card-review");
        assertThat(request.sourceId()).isEqualTo("review-completed:card-1:2026-06-02T00:00:00Z");
        assertThat(request.eventId()).isEqualTo("review-completed:card-1:2026-06-02T00:00:00Z");
    }

    @Test
    void reviewCompletedPropagatesPlatformUuidAsExternalUserIdWhileHashingInternalPk() {
        // 소스 userId가 platform UUID일 때: 내부 PK는 결정적 해시(Long)지만, externalUserId는
        // 원본 UUID를 그대로 addXp에 전달해야 outbound 이벤트가 UUID userId를 싣는다(F10).
        var uuid = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        var event = new ReviewCompleted(
                "card-9",
                uuid,
                "11112222-3333-4444-5555-666677778888",
                Rating.GOOD,
                "2026-06-10T00:00:00Z",
                "2026-06-09T00:00:00Z"
        );
        var expectedLong = com.synapse.engagement.shared.CurrentUser.resolveUserId(uuid);

        handler.handleReviewCompleted(event);

        verify(gamificationService).addXp(
                eq(expectedLong),
                eq(uuid),
                eq("11112222-3333-4444-5555-666677778888"),
                any()
        );
    }

    @Test
    void reviewCompletedWithNullUserIdIsWarnSkippedWithoutPublishingOrThrowing() {
        // 필수 신원(userId)이 없으면 outbound Avro의 non-null 필드를 채울 수 없다 — warn 후 스킵해야 한다.
        var event = new ReviewCompleted(
                "card-1",
                null,
                "tenant-learning",
                Rating.GOOD,
                "2026-06-03T00:00:00Z",
                "2026-06-02T00:00:00Z"
        );

        assertThatCode(() -> handler.handleReviewCompleted(event)).doesNotThrowAnyException();
        verify(gamificationService, never()).addXp(any(), any(), any(), any());
    }

    @Test
    void reviewCompletedWithNullTenantIdIsWarnSkippedWithoutPublishingOrThrowing() {
        // tenantId 역시 outbound Avro의 non-null 필수 필드 — null이면 warn 후 스킵한다.
        var event = new ReviewCompleted(
                "card-1",
                "800",
                null,
                Rating.GOOD,
                "2026-06-03T00:00:00Z",
                "2026-06-02T00:00:00Z"
        );

        assertThatCode(() -> handler.handleReviewCompleted(event)).doesNotThrowAnyException();
        verify(gamificationService, never()).addXp(any(), any(), any(), any());
    }

    @Test
    void reviewCompletedWithNonUuidExternalUserIdStillProcessesWithoutThrowing() {
        // 비-UUID externalUserId는 warnIfNotUuid의 warn 분기를 타지만, 비파괴적이므로 처리는 계속된다.
        var event = new ReviewCompleted(
                "card-1",
                "not-a-uuid",
                "tenant-learning",
                Rating.GOOD,
                "2026-06-03T00:00:00Z",
                "2026-06-02T00:00:00Z"
        );

        assertThatCode(() -> handler.handleReviewCompleted(event)).doesNotThrowAnyException();
        // warn은 로깅만 — addXp는 정상적으로 호출되어야 한다(externalUserId는 원본 그대로 전달).
        verify(gamificationService).addXp(
                eq(com.synapse.engagement.shared.CurrentUser.resolveUserId("not-a-uuid")),
                eq("not-a-uuid"),
                eq("tenant-learning"),
                any()
        );
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
        when(gamificationService.addXp(eq(800L), eq("800"), eq("tenant-learning"), any()))
                .thenThrow(new ConflictException("XP event already processed"));

        assertThatCode(() -> handler.handleReviewCompleted(event)).doesNotThrowAnyException();
    }
}
