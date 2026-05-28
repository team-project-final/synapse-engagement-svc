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
        return addXp(userId, "default", request);
    }

    @Transactional
    public UserGamificationResponse addXp(Long userId, String tenantId, AddXpRequest request) {
        if (xpEventRepository.existsByEventId(request.eventId())
                || xpEventRepository.existsByUserIdAndEventTypeAndSourceId(userId, request.eventType(), request.sourceId())) {
            throw new ConflictException("XP event already processed");
        }
        var amount = request.xpAmount() == null ? DEFAULT_XP : request.xpAmount();
        var profile = profileRepository.findById(userId)
                .orElseGet(() -> UserProfilesGamification.initialize(userId));
        int oldLevel = profile.getLevel();
        int newLevel = levelService.calculateLevel(profile.getTotalXp() + amount);
        profile.addXp(amount, newLevel);
        profileRepository.save(profile);
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
        if (newLevel > oldLevel) {
            eventPublisher.publishLevelUp(userId, tenantId, oldLevel, newLevel, profile.getTotalXp());
        }
        earnedBadges.forEach(badge -> eventPublisher.publishBadgeEarned(userId, tenantId, badge));
        return UserGamificationResponse.from(profile, streak, earnedBadges);
    }

    @Transactional(readOnly = true)
    public UserGamificationResponse getProfile(Long userId) {
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
