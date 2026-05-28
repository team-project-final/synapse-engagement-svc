package com.synapse.engagement.gamification.application;

import com.synapse.engagement.gamification.api.dto.BadgeResponse;
import com.synapse.engagement.gamification.domain.Badge;
import com.synapse.engagement.gamification.domain.BadgeConditionType;
import com.synapse.engagement.gamification.domain.UserBadge;
import com.synapse.engagement.gamification.domain.UserProfilesGamification;
import com.synapse.engagement.gamification.domain.UserStreak;
import com.synapse.engagement.gamification.repository.BadgeRepository;
import com.synapse.engagement.gamification.repository.UserBadgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BadgeService {
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public BadgeService(BadgeRepository badgeRepository, UserBadgeRepository userBadgeRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    @Transactional
    public List<BadgeResponse> findAll() {
        ensureDefaultBadges();
        return badgeRepository.findAll().stream()
                .map(BadgeResponse::from)
                .toList();
    }

    @Transactional
    public List<BadgeResponse> awardEligibleBadges(
            Long userId,
            UserProfilesGamification profile,
            UserStreak streak
    ) {
        ensureDefaultBadges();
        return badgeRepository.findAll().stream()
                .filter(badge -> isEligible(badge, profile, streak))
                .filter(badge -> !userBadgeRepository.existsByUserIdAndBadgeCode(userId, badge.getCode()))
                .map(badge -> userBadgeRepository.save(UserBadge.earn(userId, badge)))
                .map(BadgeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BadgeResponse> findEarnedBadges(Long userId) {
        return userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId).stream()
                .map(BadgeResponse::from)
                .toList();
    }

    private boolean isEligible(Badge badge, UserProfilesGamification profile, UserStreak streak) {
        return switch (badge.getConditionType()) {
            case TOTAL_XP -> profile.getTotalXp() >= badge.getConditionValue();
            case LEVEL -> profile.getLevel() >= badge.getConditionValue();
            case STREAK -> streak.getCurrentStreak() >= badge.getConditionValue();
        };
    }

    private void ensureDefaultBadges() {
        createDefaultBadge(
                "FIRST_XP",
                "First XP",
                "Earn XP for the first time",
                BadgeConditionType.TOTAL_XP,
                1
        );
        createDefaultBadge(
                "LEVEL_2",
                "Level 2",
                "Reach level 2",
                BadgeConditionType.LEVEL,
                2
        );
        createDefaultBadge(
                "STREAK_3",
                "3 Day Streak",
                "Keep a 3 day activity streak",
                BadgeConditionType.STREAK,
                3
        );
    }

    private void createDefaultBadge(
            String code,
            String name,
            String description,
            BadgeConditionType conditionType,
            int conditionValue
    ) {
        if (!badgeRepository.existsByCode(code)) {
            badgeRepository.save(Badge.create(code, name, description, null, conditionType, conditionValue));
        }
    }
}
