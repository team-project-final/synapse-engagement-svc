package com.synapse.engagement.community.api.dto;

import com.synapse.engagement.community.domain.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ShareContentRequest(
        @NotNull ContentType contentType,
        @NotNull Long contentId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @Size(max = 10) List<@Size(max = 30) String> tags,
        // null이면 공개 공유(기존 동작), 값이 있으면 해당 그룹의 ACTIVE 멤버에게만 보인다.
        Long groupId
) {
}
