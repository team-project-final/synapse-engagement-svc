package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfilesGamificationRepository extends JpaRepository<UserProfilesGamification, UUID> {

    default Optional<UserProfilesGamification> findByUserId(UUID userId) {
        // 이 테이블은 user_id가 곧 Primary Key라서 findById를 도메인 언어에 맞게 감싼 메서드입니다.
        return findById(userId);
    }
}

