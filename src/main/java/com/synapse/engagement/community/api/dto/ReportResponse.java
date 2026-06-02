package com.synapse.engagement.community.api.dto;

import com.synapse.engagement.community.domain.Report;
import com.synapse.engagement.community.domain.ReportStatus;
import com.synapse.engagement.community.domain.ReportTargetType;

import java.time.Instant;

public record ReportResponse(
        Long id,
        ReportTargetType targetType,
        Long targetId,
        String reason,
        ReportStatus status,
        String adminNote,
        Instant createdAt,
        Instant resolvedAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getStatus(),
                report.getAdminNote(),
                report.getCreatedAt(),
                report.getResolvedAt()
        );
    }
}
