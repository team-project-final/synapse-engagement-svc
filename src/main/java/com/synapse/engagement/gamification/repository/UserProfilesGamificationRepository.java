package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfilesGamificationRepository extends JpaRepository<UserProfilesGamification, UUID> {

    default Optional<UserProfilesGamification> findByUserId(UUID userId) {
        return findById(userId);
    }

    // Redis 장애 시 DB에서 직접 리더보드를 재구성하는 fallback 쿼리입니다.
    List<UserProfilesGamification> findAllByOrderByTotalXpDesc(Pageable pageable);
}

