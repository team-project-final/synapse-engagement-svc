package com.synapse.engagement.gamification.service;

import com.synapse.engagement.gamification.dto.response.LeaderboardEntryResponse;
import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import com.synapse.engagement.gamification.repository.UserProfilesGamificationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
class LeaderboardService {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardService.class);
    private static final String LEADERBOARD_KEY = "engagement:leaderboard";

    private final RedisTemplate<String, String> redisTemplate;
    private final UserProfilesGamificationRepository profileRepository;

    LeaderboardService(RedisTemplate<String, String> redisTemplate,
            UserProfilesGamificationRepository profileRepository) {
        this.redisTemplate = redisTemplate;
        this.profileRepository = profileRepository;
    }

    void updateScore(UUID userId, int totalXp) {
        try {
            redisTemplate.opsForZSet().add(LEADERBOARD_KEY, userId.toString(), totalXp);
        } catch (Exception e) {
            log.warn("Redis leaderboard update failed for userId={}: {}", userId, e.getMessage());
        }
    }

    List<LeaderboardEntryResponse> getTop(int limit) {
        try {
            Set<TypedTuple<String>> tuples =
                    redisTemplate.opsForZSet().reverseRangeWithScores(LEADERBOARD_KEY, 0, limit - 1);

            if (tuples != null && !tuples.isEmpty()) {
                List<LeaderboardEntryResponse> result = new ArrayList<>();
                int rank = 1;
                for (TypedTuple<String> tuple : tuples) {
                    UUID userId = UUID.fromString(tuple.getValue());
                    int xp = tuple.getScore() != null ? tuple.getScore().intValue() : 0;
                    result.add(new LeaderboardEntryResponse(rank++, userId, xp));
                }
                return result;
            }
        } catch (Exception e) {
            log.warn("Redis leaderboard read failed, falling back to DB: {}", e.getMessage());
        }

        return getTopFromDb(limit);
    }

    private List<LeaderboardEntryResponse> getTopFromDb(int limit) {
        List<UserProfilesGamification> profiles =
                profileRepository.findAllByOrderByTotalXpDesc(PageRequest.of(0, limit));

        List<LeaderboardEntryResponse> result = new ArrayList<>();
        for (int i = 0; i < profiles.size(); i++) {
            UserProfilesGamification p = profiles.get(i);
            result.add(new LeaderboardEntryResponse(i + 1, p.userId(), p.totalXp()));
        }
        return result;
    }
}
