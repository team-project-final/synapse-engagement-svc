package com.synapse.engagement.community.service;

import com.synapse.engagement.community.entity.GroupMember;
import com.synapse.engagement.community.dto.response.GroupMemberResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
// 도메인 객체(GroupMember)를 API 응답 DTO(GroupMemberResponse)로 바꿔주는 변환기입니다.
interface GroupMemberMapper {

    // GroupMember도 getter 대신 id(), role() 같은 도메인 메서드를 쓰므로 매핑식을 명시합니다.
    @Mapping(target = "id", expression = "java(member.id())")
    @Mapping(target = "groupId", expression = "java(member.groupId())")
    @Mapping(target = "userId", expression = "java(member.userId())")
    @Mapping(target = "role", expression = "java(member.role())")
    @Mapping(target = "status", expression = "java(member.status())")
    @Mapping(target = "joinedAt", expression = "java(member.joinedAt())")
    GroupMemberResponse toResponse(GroupMember member);
}

