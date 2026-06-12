package com.synapse.engagement.community.api.dto;

import com.synapse.engagement.community.domain.ContentType;
import com.synapse.engagement.community.domain.SharedContent;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record SharedContentResponse(
        Long id,
        String shareToken,
        ContentType contentType,
        Long contentId,
        Long ownerId,
        String title,
        String description,
        List<String> tags,
        long downloadCount,
        Long sourceShareId,
        Instant createdAt
) {
    public static SharedContentResponse from(SharedContent content) {
        return new SharedContentResponse(
                content.getId(),
                content.getShareToken(),
                content.getContentType(),
                content.getContentId(),
                content.getOwnerId(),
                content.getTitle(),
                content.getDescription(),
                splitTags(content.getTags()),
                content.getDownloadCount(),
                content.getSourceShareId(),
                content.getCreatedAt()
        );
    }

    private static List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .toList();
    }
}
