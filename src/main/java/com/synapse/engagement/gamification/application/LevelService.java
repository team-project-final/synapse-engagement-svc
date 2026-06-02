package com.synapse.engagement.gamification.application;

import org.springframework.stereotype.Service;

@Service
public class LevelService {
    public int calculateLevel(int totalXp) {
        // 레벨 구간은 현재 고정 정책이다. 시즌제/동적 정책이 생기면 이 Service가 교체 지점이 된다.
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
