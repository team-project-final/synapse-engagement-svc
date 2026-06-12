package com.synapse.engagement.gamification.repository;

import com.synapse.engagement.gamification.entity.LevelDefinition;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LevelDefinitionRepository extends JpaRepository<LevelDefinition, UUID> {

    // totalXp 이하의 min_xp 중 가장 큰 것 → 현재 레벨
    @Query("SELECT ld FROM LevelDefinition ld WHERE ld.minXp <= :totalXp ORDER BY ld.minXp DESC LIMIT 1")
    Optional<LevelDefinition> findCurrentLevel(int totalXp);

    Optional<LevelDefinition> findByLevelNumber(int levelNumber);

    @Query("SELECT ld FROM LevelDefinition ld ORDER BY ld.levelNumber DESC LIMIT 1")
    Optional<LevelDefinition> findMaxLevel();
}
