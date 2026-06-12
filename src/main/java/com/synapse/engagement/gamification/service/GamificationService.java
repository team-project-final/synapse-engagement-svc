package com.synapse.engagement.gamification.service;

import com.synapse.engagement.gamification.dto.response.LeaderboardEntryResponse;
import com.synapse.engagement.gamification.dto.response.UserXpResponse;
import com.synapse.engagement.gamification.dto.response.XpEventResponse;
import com.synapse.engagement.gamification.entity.UserBadge;
import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import com.synapse.engagement.gamification.entity.XpEvent;
import com.synapse.engagement.gamification.repository.UserProfilesGamificationRepository;
import com.synapse.engagement.gamification.repository.XpEventRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamificationService {

    private final UserProfilesGamificationRepository profileRepository;
    private final XpEventRepository xpEventRepository;
    private final GamificationMapper gamificationMapper;
    private final LevelService levelService;
    private final BadgeService badgeService;
    private final StreakService streakService;
    private final LeaderboardService leaderboardService;

    GamificationService(
            UserProfilesGamificationRepository profileRepository,
            XpEventRepository xpEventRepository,
            GamificationMapper gamificationMapper,
            LevelService levelService,
            BadgeService badgeService,
            StreakService streakService,
            LeaderboardService leaderboardService) {
        this.profileRepository = profileRepository;
        this.xpEventRepository = xpEventRepository;
        this.gamificationMapper = gamificationMapper;
        this.levelService = levelService;
        this.badgeService = badgeService;
        this.streakService = streakService;
        this.leaderboardService = leaderboardService;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserXpResponse addXp(AddXpCommand command) {
        if (isDuplicate(command)) {
            return getProfile(command.userId());
        }

        UserProfilesGamification profile = profileRepository.findByUserId(command.userId())
                .orElseGet(() -> profileRepository.save(UserProfilesGamification.create(command.userId())));

        XpEvent event = XpEvent.create(
                command.userId(),
                command.eventType(),
                command.xpAmount(),
                command.sourceId(),
                command.sourceType(),
                command.eventId());

        xpEventRepository.save(event);
        profile.addXp(command.xpAmount());

        // Step 6: 스트릭 → 레벨(DB 기반) → 배지 순서로 갱신합니다.
        streakService.updateStreak(profile);
        LevelInfo levelInfo = levelService.applyAndGet(profile);
        badgeService.evaluateAndAward(profile);
        leaderboardService.updateScore(profile.userId(), profile.totalXp());

        List<UserBadge> recentBadges = badgeService.getRecentBadges(profile.userId());
        return gamificationMapper.toProfileResponse(profile, levelInfo, recentBadges);
    }

    @Transactional(readOnly = true)
    public UserXpResponse getProfile(UUID userId) {
        UserProfilesGamification profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfilesGamification.create(userId));

        LevelInfo levelInfo = levelService.getInfo(profile.level(), profile.totalXp());
        List<UserBadge> recentBadges = badgeService.getRecentBadges(userId);
        return gamificationMapper.toProfileResponse(profile, levelInfo, recentBadges);
    }

    @Transactional(readOnly = true)
    public List<XpEventResponse> getXpHistory(UUID userId, int size) {
        return gamificationMapper.toEventResponses(xpEventRepository.findByUserIdOrderByCreatedAtDesc(userId, size));
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard(int limit) {
        return leaderboardService.getTop(limit);
    }

    private boolean isDuplicate(AddXpCommand command) {
        return xpEventRepository.existsByEventId(command.eventId())
                || xpEventRepository.existsByUserIdAndEventTypeAndSourceId(
                        command.userId(),
                        command.eventType(),
                        command.sourceId());
    }
}
