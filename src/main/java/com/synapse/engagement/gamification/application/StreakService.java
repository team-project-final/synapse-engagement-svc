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
        streak.recordActivity(LocalDate.now());
        return userStreakRepository.save(streak);
    }

    @Transactional(readOnly = true)
    public UserStreak findOrInitialize(Long userId) {
        return userStreakRepository.findByUserId(userId)
                .orElseGet(() -> UserStreak.initialize(userId));
    }
}
