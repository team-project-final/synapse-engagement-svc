package com.synapse.engagement.gamification.application.event;

import com.synapse.engagement.gamification.api.dto.AddXpRequest;
import com.synapse.engagement.gamification.application.GamificationService;
import com.synapse.engagement.gamification.domain.EventType;
import com.synapse.engagement.gamification.domain.UserProfilesGamification;
import com.synapse.engagement.gamification.repository.UserProfilesGamificationRepository;
import com.synapse.engagement.shared.ConflictException;
import com.synapse.engagement.shared.CurrentUser;
import com.synapse.learning.ReviewCompleted;
import com.synapse.platform.UserRegistered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EngagementKafkaEventHandler {
    private static final Logger log = LoggerFactory.getLogger(EngagementKafkaEventHandler.class);

    private final UserProfilesGamificationRepository profileRepository;
    private final GamificationService gamificationService;

    public EngagementKafkaEventHandler(
            UserProfilesGamificationRepository profileRepository,
            GamificationService gamificationService
    ) {
        this.profileRepository = profileRepository;
        this.gamificationService = gamificationService;
    }

    @Transactional
    public void handleUserRegistered(UserRegistered event) {
        var userId = resolveUserId(event.getUserId());
        if (profileRepository.existsById(userId)) {
            log.info("UserRegistered already has gamification profile. userId={}", userId);
            return;
        }
        profileRepository.save(UserProfilesGamification.initialize(userId));
        log.info("Created gamification profile from UserRegistered. userId={}, tenantId={}", userId, event.getTenantId());
    }

    public void handleReviewCompleted(ReviewCompleted event) {
        var userId = resolveUserId(event.getUserId());
        var tenantId = event.getTenantId().toString();
        var eventId = reviewCompletedIdempotencyKey(event);
        var request = new AddXpRequest(
                EventType.CARD_REVIEWED,
                null,
                eventId,
                "card-review",
                eventId
        );
        try {
            gamificationService.addXp(userId, tenantId, request);
            log.info("Applied XP from ReviewCompleted. userId={}, tenantId={}, cardId={}", userId, tenantId, event.getCardId());
        } catch (ConflictException ex) {
            log.info("ReviewCompleted already processed. userId={}, eventId={}", userId, eventId);
        }
    }

    private String reviewCompletedIdempotencyKey(ReviewCompleted event) {
        // shared ReviewCompleted에는 eventId/reviewId가 없으므로 cardId+reviewedAt 조합을 멱등성 키로 사용한다.
        return "review-completed:%s:%s".formatted(event.getCardId(), event.getReviewedAt());
    }

    private Long resolveUserId(CharSequence externalUserId) {
        // HTTP 경로(CurrentUser.require)와 동일한 도출을 공유해 신원이 분기되지 않도록 한다.
        return CurrentUser.resolveUserId(externalUserId == null ? null : externalUserId.toString());
    }
}
