package com.synapse.engagement.gamification.application;

import com.synapse.engagement.gamification.domain.UserStreak;
import com.synapse.engagement.gamification.repository.UserStreakRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class StreakService {
    private final UserStreakRepository userStreakRepository;

    public StreakService(UserStreakRepository userStreakRepository) {
        this.userStreakRepository = userStreakRepository;
    }

    @Transactional
    public UserStreak recordActivity(Long userId) {
        var streak = findOrInitialize(userId);
        // 연속 활동 계산 규칙은 UserStreak 도메인 객체 안에 두고, Service는 오늘 활동을 기록하는 오케스트레이션만 맡는다.
        streak.recordActivity(LocalDate.now());
        return userStreakRepository.save(streak);
    }

    @Transactional(readOnly = true)
    public UserStreak findOrInitialize(Long userId) {
        return userStreakRepository.findByUserId(userId)
                .orElseGet(() -> UserStreak.initialize(userId));
    }
}
