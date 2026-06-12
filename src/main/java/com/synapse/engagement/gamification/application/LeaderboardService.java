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
        // 클라이언트가 과도한 limit을 보내도 DB 조회 범위는 1~100 안으로 고정한다.
        var pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 100));
        // XP 동률일 때 userId 오름차순으로 정렬해 같은 데이터에 항상 같은 순위 결과가 나오게 한다.
        return profileRepository.findByOrderByTotalXpDescUserIdAsc(pageable).stream()
                .map(profile -> LeaderboardEntryResponse.from(rank.getAndIncrement(), profile))
                .toList();
    }
}
