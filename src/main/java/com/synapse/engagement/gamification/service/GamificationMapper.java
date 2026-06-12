package com.synapse.engagement.gamification.service;

import com.synapse.engagement.gamification.dto.response.BadgeResponse;
import com.synapse.engagement.gamification.dto.response.UserXpResponse;
import com.synapse.engagement.gamification.dto.response.XpEventResponse;
import com.synapse.engagement.gamification.entity.UserBadge;
import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import com.synapse.engagement.gamification.entity.XpEvent;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface GamificationMapper {

    @Mapping(target = "id", expression = "java(event.id())")
    @Mapping(target = "eventType", expression = "java(event.eventType())")
    @Mapping(target = "xpAmount", expression = "java(event.xpAmount())")
    @Mapping(target = "sourceId", expression = "java(event.sourceId())")
    @Mapping(target = "sourceType", expression = "java(event.sourceType())")
    @Mapping(target = "createdAt", expression = "java(event.createdAt())")
    XpEventResponse toResponse(XpEvent event);

    List<XpEventResponse> toEventResponses(List<XpEvent> events);

    default UserXpResponse toProfileResponse(
            UserProfilesGamification profile,
            LevelInfo levelInfo,
            List<UserBadge> recentBadges) {
        return new UserXpResponse(
                profile.userId(),
                levelInfo.level(),
                profile.totalXp(),
                profile.currentStreak(),
                profile.longestStreak(),
                levelInfo.title(),
                levelInfo.nextLevelXp(),
                recentBadges.stream().map(this::toBadgeResponse).toList());
    }

    default BadgeResponse toBadgeResponse(UserBadge userBadge) {
        return new BadgeResponse(
                userBadge.badge().code(),
                userBadge.badge().name(),
                userBadge.earnedAt());
    }
}
