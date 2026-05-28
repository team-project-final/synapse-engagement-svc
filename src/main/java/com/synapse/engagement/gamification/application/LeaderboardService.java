package com.synapse.engagement.gamification.application;

import com.synapse.engagement.gamification.api.dto.LeaderboardEntryResponse;
import com.synapse.engagement.gamification.repository.UserProfilesGamificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LeaderboardService {
    private final UserProfilesGamificationRepository profileRepository;

    public LeaderboardService(UserProfilesGamificationRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> findLeaderboard(int limit) {
        var rank = new AtomicInteger(1);
        var pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 100));
        return profileRepository.findByOrderByTotalXpDescUserIdAsc(pageable).stream()
                .map(profile -> LeaderboardEntryResponse.from(rank.getAndIncrement(), profile))
                .toList();
    }
}
