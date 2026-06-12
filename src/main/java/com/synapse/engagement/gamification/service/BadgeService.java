package com.synapse.engagement.gamification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.engagement.gamification.entity.Badge;
import com.synapse.engagement.gamification.entity.UserBadge;
import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import com.synapse.engagement.gamification.repository.BadgeRepository;
import com.synapse.engagement.gamification.repository.UserBadgeRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class BadgeService {

    private static final Logger log = LoggerFactory.getLogger(BadgeService.class);

    private final BadgeRepository badgeRepo;
    private final UserBadgeRepository userBadgeRepo;
    private final ObjectMapper objectMapper;

    BadgeService(BadgeRepository badgeRepo, UserBadgeRepository userBadgeRepo, ObjectMapper objectMapper) {
        this.badgeRepo = badgeRepo;
        this.userBadgeRepo = userBadgeRepo;
        this.objectMapper = objectMapper;
    }

    List<UserBadge> evaluateAndAward(UserProfilesGamification profile) {
        Set<String> alreadyEarned = Set.copyOf(userBadgeRepo.findBadgeCodesByUserId(profile.userId()));

        return badgeRepo.findAll().stream()
                .filter(badge -> !alreadyEarned.contains(badge.code()))
                .filter(badge -> meetsCondition(badge, profile))
                .map(badge -> {
                    UserBadge ub = UserBadge.award(profile.userId(), badge);
                    return userBadgeRepo.save(ub);
                })
                .collect(Collectors.toList());
    }

    private boolean meetsCondition(Badge badge, UserProfilesGamification profile) {
        try {
            JsonNode node = objectMapper.readTree(badge.criteriaJson());
            String type = node.get("type").asText();
            int value = node.get("value").asInt();
            return switch (type) {
                case "xp_threshold"     -> profile.totalXp() >= value;
                case "streak_threshold" -> profile.currentStreak() >= value;
                case "level_threshold"  -> profile.level() >= value;
                default -> {
                    log.warn("Unknown badge criteria type: {}", type);
                    yield false;
                }
            };
        } catch (Exception e) {
            log.warn("Failed to evaluate badge criteria for badge {}: {}", badge.code(), e.getMessage());
            return false;
        }
    }

    List<UserBadge> getRecentBadges(UUID userId) {
        return userBadgeRepo.findTop5ByUserIdOrderByEarnedAtDesc(userId);
    }
}
