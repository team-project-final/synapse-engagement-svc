package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.domain.UserProfilesGamification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProfilesGamificationRepository extends JpaRepository<UserProfilesGamification, Long> {
    List<UserProfilesGamification> findByOrderByTotalXpDescUserIdAsc(Pageable pageable);
}
