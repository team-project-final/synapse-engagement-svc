package com.synapse.engagement.community.dto.request;

import com.synapse.engagement.community.entity.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record ShareContentRequest(
        @NotNull ContentType contentType,
        @NotNull UUID contentId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @Size(max = 10) List<@Size(max = 40) String> tags) {
}

