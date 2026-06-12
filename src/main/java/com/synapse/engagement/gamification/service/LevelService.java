package com.synapse.engagement.gamification.service;

import com.synapse.engagement.gamification.entity.LevelDefinition;
import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import com.synapse.engagement.gamification.repository.LevelDefinitionRepository;
import org.springframework.stereotype.Service;

@Service
class LevelService {

    private final LevelDefinitionRepository levelRepo;

    LevelService(LevelDefinitionRepository levelRepo) {
        this.levelRepo = levelRepo;
    }

    LevelInfo applyAndGet(UserProfilesGamification profile) {
        int totalXp = profile.totalXp();

        LevelDefinition current = levelRepo.findCurrentLevel(totalXp)
                .orElse(null);

        if (current == null) {
            return new LevelInfo(profile.level(), profile.title(), profile.nextLevelXp());
        }

        profile.applyLevel(current.levelNumber(), current.title());

        int nextLevelXp = levelRepo.findByLevelNumber(current.levelNumber() + 1)
                .map(LevelDefinition::minXp)
                .orElse(current.minXp());

        return new LevelInfo(current.levelNumber(), current.title(), nextLevelXp);
    }

    LevelInfo getInfo(int currentLevel, int totalXp) {
        LevelDefinition current = levelRepo.findByLevelNumber(currentLevel)
                .or(() -> levelRepo.findCurrentLevel(totalXp))
                .orElse(null);

        if (current == null) {
            int simpleNextXp = currentLevel * 100;
            return new LevelInfo(currentLevel, "Novice", simpleNextXp);
        }

        int nextLevelXp = levelRepo.findByLevelNumber(current.levelNumber() + 1)
                .map(LevelDefinition::minXp)
                .orElse(current.minXp());

        return new LevelInfo(current.levelNumber(), current.title(), nextLevelXp);
    }
}
