package com.synapse.engagement.gamification.dto.response;

import java.util.List;
import java.util.UUID;

/*
 * GET /api/v1/gamification/profile 응답 DTO입니다.
 * 화면에서는 이 값만 보면 사용자의 현재 XP 상태를 그릴 수 있습니다.
 */
public record UserXpResponse(
        // 현재 조회 중인 사용자 ID입니다.
        UUID userId,
        // 총 XP를 기준으로 계산된 현재 레벨입니다.
        int level,
        // 지금까지 누적된 전체 XP입니다.
        int totalXp,
        // 연속 학습 일수입니다. Step 4에서는 기본값 0이고 Step 6에서 갱신 로직이 들어옵니다.
        int currentStreak,
        // 사용자가 달성한 최장 연속 학습 일수입니다.
        int longestStreak,
        // 레벨 구간에 따라 보여줄 간단한 칭호입니다.
        String title,
        // 다음 레벨에 도달하기 위해 필요한 총 XP 기준점입니다.
        int nextLevelXp,
        // 최근 획득 배지 목록입니다. Step 4에서는 빈 목록이고 Step 6에서 실제 값이 들어옵니다.
        List<String> recentBadges) {
}

