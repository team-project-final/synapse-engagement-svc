package io.synapse.community.group.service;

import io.synapse.community.group.entity.Group;
import io.synapse.community.group.dto.GroupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
// 도메인 객체(Group)를 API 응답 DTO(GroupResponse)로 바꿔주는 변환기입니다.
public interface GroupMapper {

    // Group은 getter 대신 id(), name() 같은 도메인 메서드를 쓰므로 MapStruct에 매핑식을 명시합니다.
    @Mapping(target = "id", expression = "java(group.id())")
    @Mapping(target = "name", expression = "java(group.name())")
    @Mapping(target = "description", expression = "java(group.description())")
    @Mapping(target = "isPublic", expression = "java(group.isPublic())")
    @Mapping(target = "ownerId", expression = "java(group.ownerId())")
    @Mapping(target = "createdAt", expression = "java(group.createdAt())")
    GroupResponse toResponse(Group group);
}
