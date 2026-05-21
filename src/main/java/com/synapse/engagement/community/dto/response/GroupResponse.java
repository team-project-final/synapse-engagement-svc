package com.synapse.engagement.community.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupResponse(
        UUID id,
        String name,
        String description,
        boolean isPublic,
        UUID ownerId,
        LocalDateTime createdAt) {
}

