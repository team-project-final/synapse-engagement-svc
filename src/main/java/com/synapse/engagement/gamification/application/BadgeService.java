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
        // 별도 seed 마이그레이션 없이도 dev/test 환경에서 기본 배지 목록이 항상 존재하게 보정한다.
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
        // 모든 배지를 조건식으로 평가한 뒤, 이미 받은 배지는 제외해 배지 수여를 멱등하게 만든다.
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
        // BadgeConditionType이 새로 늘어나면 이 switch가 컴파일 단계에서 빠진 조건을 드러낸다.
        return switch (badge.getConditionType()) {
            case TOTAL_XP -> profile.getTotalXp() >= badge.getConditionValue();
            case LEVEL -> profile.getLevel() >= badge.getConditionValue();
            case STREAK -> streak.getCurrentStreak() >= badge.getConditionValue();
        };
    }

    private void ensureDefaultBadges() {
        // 기본 배지는 코드(code)를 자연키처럼 사용한다. 중복 생성 방지는 repository existsByCode가 담당한다.
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
