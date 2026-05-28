package com.synapse.engagement.gamification.api.dto;

import com.synapse.engagement.gamification.domain.Badge;
import com.synapse.engagement.gamification.domain.BadgeConditionType;
import com.synapse.engagement.gamification.domain.UserBadge;

import java.time.Instant;

public record BadgeResponse(
        String code,
        String name,
        String description,
        String iconUrl,
        BadgeConditionType conditionType,
        int conditionValue,
        Instant earnedAt
) {
    public static BadgeResponse from(Badge badge) {
        return new BadgeResponse(
                badge.getCode(),
                badge.getName(),
                badge.getDescription(),
                badge.getIconUrl(),
                badge.getConditionType(),
                badge.getConditionValue(),
                null
        );
    }

    public static BadgeResponse from(UserBadge userBadge) {
        var badge = userBadge.getBadge();
        return new BadgeResponse(
                badge.getCode(),
                badge.getName(),
                badge.getDescription(),
                badge.getIconUrl(),
                badge.getConditionType(),
                badge.getConditionValue(),
                userBadge.getEarnedAt()
        );
    }
}
