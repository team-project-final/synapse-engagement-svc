package com.synapse.engagement.community.api.dto;

import com.synapse.engagement.community.domain.Group;

import java.time.Instant;

public record GroupResponse(
        Long id,
        String name,
        String description,
        boolean isPublic,
        Long ownerId,
        Instant createdAt
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.isPublicGroup(),
                group.getOwnerId(),
                group.getCreatedAt()
        );
    }
}
