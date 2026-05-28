package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    boolean existsByUserIdAndBadgeCode(Long userId, String badgeCode);

    List<UserBadge> findByUserIdOrderByEarnedAtDesc(Long userId);
}
