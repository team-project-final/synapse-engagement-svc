package com.synapse.engagement.community.service;

import com.synapse.engagement.community.dto.response.SharedContentResponse;
import com.synapse.engagement.community.entity.SharedContent;
import org.springframework.stereotype.Component;

@Component
class SharedContentMapper {

    SharedContentResponse toResponse(SharedContent content) {
        return new SharedContentResponse(
                content.id(),
                content.shareToken(),
                content.contentType(),
                content.contentId(),
                content.ownerId(),
                content.title(),
                content.description(),
                content.tags(),
                content.downloadCount(),
                content.createdAt());
    }
}

