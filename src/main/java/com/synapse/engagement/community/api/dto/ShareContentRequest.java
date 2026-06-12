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
        @Size(max = 10) List<@Size(max = 30) String> tags
) {
}
