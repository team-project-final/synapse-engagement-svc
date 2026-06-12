package com.synapse.engagement.gamification.service;

import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;

@Service
class StreakService {

    // 스트릭 기준 시간대는 KST(UTC+9)로 고정합니다. 자정이 지나야 연속 일수가 증가합니다.
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    void updateStreak(UserProfilesGamification profile) {
        LocalDate today = LocalDate.now(KST);
        profile.updateStreak(today);
    }
}
