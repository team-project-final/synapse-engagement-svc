package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.domain.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    boolean existsByCode(String code);

    Optional<Badge> findByCode(String code);
}
