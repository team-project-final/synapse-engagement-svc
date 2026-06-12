package com.synapse.engagement.gamification.service;

// LevelService가 계산한 레벨 결과를 GamificationService와 Mapper로 전달하는 값 객체입니다.
record LevelInfo(int level, String title, int nextLevelXp) {
}
