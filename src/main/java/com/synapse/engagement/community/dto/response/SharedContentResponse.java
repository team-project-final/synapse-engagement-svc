package com.synapse.engagement.community.dto.response;

import com.synapse.engagement.community.entity.ContentType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SharedContentResponse(
        UUID id,
        String shareToken,
        ContentType contentType,
        UUID contentId,
        UUID ownerId,
        String title,
        String description,
        List<String> tags,
        int downloadCount,
        LocalDateTime createdAt) {
}

