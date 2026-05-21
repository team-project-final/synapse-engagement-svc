package com.synapse.engagement.gamification.service;

import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import com.synapse.engagement.gamification.entity.XpEvent;
import com.synapse.engagement.gamification.dto.response.UserXpResponse;
import com.synapse.engagement.gamification.dto.response.XpEventResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface GamificationMapper {

    /*
     * Entity를 API 응답 DTO로 바꾸는 Mapper입니다.
     * Entity를 그대로 Controller 밖으로 내보내지 않으면, DB 구조가 API 계약에 직접 노출되는 것을 줄일 수 있습니다.
     */
    @Mapping(target = "userId", expression = "java(profile.userId())")
    @Mapping(target = "level", expression = "java(profile.level())")
    @Mapping(target = "totalXp", expression = "java(profile.totalXp())")
    @Mapping(target = "currentStreak", expression = "java(profile.currentStreak())")
    @Mapping(target = "longestStreak", expression = "java(profile.longestStreak())")
    @Mapping(target = "title", expression = "java(profile.title())")
    @Mapping(target = "nextLevelXp", expression = "java(profile.nextLevelXp())")
    // Step 4에는 배지 기능이 아직 없으므로 빈 목록을 내려주고, Step 6에서 실제 배지 목록으로 교체합니다.
    @Mapping(target = "recentBadges", expression = "java(java.util.List.of())")
    UserXpResponse toResponse(UserProfilesGamification profile);

    // XP 이력 응답에는 멱등성용 eventId는 노출하지 않고, 사용자에게 필요한 활동 정보만 제공합니다.
    @Mapping(target = "id", expression = "java(event.id())")
    @Mapping(target = "eventType", expression = "java(event.eventType())")
    @Mapping(target = "xpAmount", expression = "java(event.xpAmount())")
    @Mapping(target = "sourceId", expression = "java(event.sourceId())")
    @Mapping(target = "sourceType", expression = "java(event.sourceType())")
    @Mapping(target = "createdAt", expression = "java(event.createdAt())")
    XpEventResponse toResponse(XpEvent event);

    List<XpEventResponse> toEventResponses(List<XpEvent> events);
}

