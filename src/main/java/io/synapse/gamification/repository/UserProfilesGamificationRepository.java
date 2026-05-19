package io.synapse.gamification.repository;

import io.synapse.gamification.entity.UserProfilesGamification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfilesGamificationRepository extends JpaRepository<UserProfilesGamification, UUID> {

    default Optional<UserProfilesGamification> findByUserId(UUID userId) {
        return findById(userId);
    }
}
