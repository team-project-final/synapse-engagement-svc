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

import java.util.UUID;

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
        // 원본 ReviewCompleted.userId는 이미 platform UUID다. 내부 PK는 해시 Long(userId)을 쓰되,
        // outbound 이벤트에는 원본 UUID(externalUserId)를 그대로 실어 platform UUID.fromString 실패(F10)를 막는다.
        var externalUserId = event.getUserId() == null ? null : event.getUserId().toString();
        var tenantId = event.getTenantId() == null ? null : event.getTenantId().toString();
        // userId/tenantId는 outbound Avro(LevelUp/BadgeEarned/NotificationSend)의 non-null 필수 필드다.
        // null이면 이벤트는 신원 없이 처리 불가 — Avro .build()가 ConflictException 밖에서 터져 컨슈머를
        // 막기 전에 여기서 WARN 후 스킵한다(warn-and-skip).
        if (externalUserId == null || tenantId == null) {
            log.warn("Skipping ReviewCompleted with missing identity. externalUserId={}, tenantId={}, cardId={}",
                    externalUserId, tenantId, event.getCardId());
            return;
        }
        var userId = resolveUserId(event.getUserId());
        warnIfNotUuid("externalUserId", externalUserId);
        warnIfNotUuid("tenantId", tenantId);
        var eventId = reviewCompletedIdempotencyKey(event);
        var request = new AddXpRequest(
                EventType.CARD_REVIEWED,
                null,
                eventId,
                "card-review",
                eventId
        );
        try {
            gamificationService.addXp(userId, externalUserId, tenantId, request);
            log.info("Applied XP from ReviewCompleted. userId={}, externalUserId={}, tenantId={}, cardId={}",
                    userId, externalUserId, tenantId, event.getCardId());
        } catch (ConflictException ex) {
            log.info("ReviewCompleted already processed. userId={}, eventId={}", userId, eventId);
        }
    }

    private void warnIfNotUuid(String field, String value) {
        // platform 측 소비자가 UUID.fromString(tenantId/userId) 하므로, 비-UUID가 흘러오면 방어 로깅한다(F10).
        if (value == null) {
            return;
        }
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            log.warn("Outbound {} is not a UUID — platform UUID.fromString may fail. {}={}", field, field, value);
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
