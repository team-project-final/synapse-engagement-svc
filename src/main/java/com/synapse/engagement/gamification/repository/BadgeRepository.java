package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.entity.Badge;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, UUID> {
}
