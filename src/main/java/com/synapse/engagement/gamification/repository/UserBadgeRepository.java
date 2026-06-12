package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.entity.UserBadge;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {

    List<UserBadge> findByUserIdOrderByEarnedAtDesc(UUID userId, Pageable pageable);

    default List<UserBadge> findTop5ByUserIdOrderByEarnedAtDesc(UUID userId) {
        return findByUserIdOrderByEarnedAtDesc(userId, PageRequest.of(0, 5));
    }

    boolean existsByUserIdAndBadge_Code(UUID userId, String badgeCode);

    @Query("SELECT ub.badge.code FROM UserBadge ub WHERE ub.userId = :userId")
    List<String> findBadgeCodesByUserId(@Param("userId") UUID userId);
}
