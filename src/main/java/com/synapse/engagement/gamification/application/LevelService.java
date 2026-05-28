package com.synapse.engagement.gamification.application;

import org.springframework.stereotype.Service;

@Service
public class LevelService {
    public int calculateLevel(int totalXp) {
        if (totalXp < 100) {
            return 1;
        }
        if (totalXp < 300) {
            return 2;
        }
        if (totalXp < 600) {
            return 3;
        }
        return 4 + ((totalXp - 600) / 500);
    }
}
