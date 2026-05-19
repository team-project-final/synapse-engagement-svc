package io.synapse.gamification.service;

import io.synapse.gamification.entity.UserProfilesGamification;
import io.synapse.gamification.entity.XpEvent;
import io.synapse.gamification.dto.UserXpResponse;
import io.synapse.gamification.dto.XpEventResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface GamificationMapper {

    @Mapping(target = "userId", expression = "java(profile.userId())")
    @Mapping(target = "level", expression = "java(profile.level())")
    @Mapping(target = "totalXp", expression = "java(profile.totalXp())")
    @Mapping(target = "currentStreak", expression = "java(profile.currentStreak())")
    @Mapping(target = "longestStreak", expression = "java(profile.longestStreak())")
    @Mapping(target = "title", expression = "java(profile.title())")
    @Mapping(target = "nextLevelXp", expression = "java(profile.nextLevelXp())")
    @Mapping(target = "recentBadges", expression = "java(java.util.List.of())")
    UserXpResponse toResponse(UserProfilesGamification profile);

    @Mapping(target = "id", expression = "java(event.id())")
    @Mapping(target = "eventType", expression = "java(event.eventType())")
    @Mapping(target = "xpAmount", expression = "java(event.xpAmount())")
    @Mapping(target = "sourceId", expression = "java(event.sourceId())")
    @Mapping(target = "sourceType", expression = "java(event.sourceType())")
    @Mapping(target = "createdAt", expression = "java(event.createdAt())")
    XpEventResponse toResponse(XpEvent event);

    List<XpEventResponse> toEventResponses(List<XpEvent> events);
}
