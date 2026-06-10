package com.synapse.engagement.gamification.application;

import com.synapse.engagement.gamification.api.dto.AddXpRequest;
import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import com.synapse.engagement.gamification.api.dto.UserGamificationResponse;
import com.synapse.engagement.gamification.api.dto.XpEventResponse;
import com.synapse.engagement.gamification.application.event.GamificationEventPublisher;
import com.synapse.engagement.gamification.domain.UserProfilesGamification;
import com.synapse.engagement.gamification.domain.XpEvent;
import com.synapse.engagement.gamification.repository.UserProfilesGamificationRepository;
import com.synapse.engagement.gamification.repository.XpEventRepository;
import com.synapse.engagement.shared.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GamificationService {
    private static final int DEFAULT_XP = 10;

    private final XpEventRepository xpEventRepository;
    private final UserProfilesGamificationRepository profileRepository;
    private final BadgeService badgeService;
    private final LevelService levelService;
    private final StreakService streakService;
    private final GamificationEventPublisher eventPublisher;

    public GamificationService(
            XpEventRepository xpEventRepository,
            UserProfilesGamificationRepository profileRepository,
            BadgeService badgeService,
            LevelService levelService,
            StreakService streakService,
            GamificationEventPublisher eventPublisher
    ) {
        this.xpEventRepository = xpEventRepository;
        this.profileRepository = profileRepository;
        this.badgeService = badgeService;
        this.levelService = levelService;
        this.streakService = streakService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UserGamificationResponse addXp(Long userId, AddXpRequest request) {
        return addXp(userId, String.valueOf(userId), "default", request);
    }

    /**
     * @param userId         내부 PK(Long). XP/프로필/멱등성 등 모든 내부 처리에 사용.
     * @param externalUserId platform UUID 문자열(원본 이벤트 userId 또는 JWT subject).
     *                       outbound 이벤트(LevelUp/BadgeEarned/NotificationSend)에 그대로 실린다(F10).
     */
    @Transactional
    public UserGamificationResponse addXp(Long userId, String externalUserId, String tenantId, AddXpRequest request) {
        // XP는 외부 이벤트 재전달이나 같은 source 중복 요청이 들어와도 한 번만 적립되어야 한다.
        // eventId는 Kafka/외부 이벤트 멱등성 키, userId+eventType+sourceId는 도메인 중복 방어선이다.
        if (xpEventRepository.existsByEventId(request.eventId())
                || xpEventRepository.existsByUserIdAndEventTypeAndSourceId(userId, request.eventType(), request.sourceId())) {
            throw new ConflictException("XP event already processed");
        }
        var amount = request.xpAmount() == null ? DEFAULT_XP : request.xpAmount();
        var profile = profileRepository.findById(userId)
                .orElseGet(() -> UserProfilesGamification.initialize(userId));
        // 레벨은 저장된 프로필 상태를 직접 추측하지 않고, "적립 후 총 XP"를 기준으로 다시 계산한다.
        int oldLevel = profile.getLevel();
        int newLevel = levelService.calculateLevel(profile.getTotalXp() + amount);
        profile.addXp(amount, newLevel);
        profileRepository.save(profile);
        // XP 적립, 스트릭 갱신, 이력 저장, 배지 평가가 한 트랜잭션에 묶여야 사용자 상태가 서로 어긋나지 않는다.
        var streak = streakService.recordActivity(userId);
        xpEventRepository.save(XpEvent.create(
                userId,
                request.eventType(),
                amount,
                request.sourceId(),
                request.sourceType(),
                request.eventId()
        ));
        var earnedBadges = badgeService.awardEligibleBadges(userId, profile, streak);
        // DB 상태 변경 후 도메인 결과가 확정된 경우에만 downstream 알림용 이벤트를 발행한다.
        if (newLevel > oldLevel) {
            eventPublisher.publishLevelUp(userId, externalUserId, tenantId, oldLevel, newLevel, profile.getTotalXp());
        }
        earnedBadges.forEach(badge -> eventPublisher.publishBadgeEarned(userId, externalUserId, tenantId, badge));
        return UserGamificationResponse.from(profile, streak, earnedBadges);
    }

    @Transactional(readOnly = true)
    public UserGamificationResponse getProfile(Long userId) {
        // 프로필/스트릭이 아직 없어도 조회 API는 빈 초기 상태를 보여준다. 실제 저장은 적립 시점에만 한다.
        var profile = profileRepository.findById(userId)
                .orElseGet(() -> UserProfilesGamification.initialize(userId));
        var streak = streakService.findOrInitialize(userId);
        return UserGamificationResponse.from(profile, streak, badgeService.findEarnedBadges(userId));
    }

    @Transactional(readOnly = true)
    public List<XpEventResponse> getXpHistory(Long userId) {
        return xpEventRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(XpEventResponse::from)
                .toList();
    }
}
