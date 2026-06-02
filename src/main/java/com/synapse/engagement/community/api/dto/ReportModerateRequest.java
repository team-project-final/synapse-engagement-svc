package com.synapse.engagement.community.api.dto;

import com.synapse.engagement.community.domain.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportModerateRequest(
        @NotNull ReportStatus status,
        @Size(max = 1000) String adminNote
) {
}
