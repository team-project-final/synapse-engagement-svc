package com.synapse.engagement.community.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        boolean isPublic
) {
}
