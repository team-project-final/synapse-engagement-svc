package com.synapse.engagement.community.api.dto;

import com.synapse.engagement.community.domain.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        @NotNull ReportTargetType targetType,
        @NotNull @Positive Long targetId,
        @NotBlank @Size(max = 1000) String reason
) {
}
