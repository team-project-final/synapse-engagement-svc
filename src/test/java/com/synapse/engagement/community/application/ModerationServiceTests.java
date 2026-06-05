package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.ReportModerateRequest;
import com.synapse.engagement.community.application.event.CommunityNotificationPublisher;
import com.synapse.engagement.community.domain.Report;
import com.synapse.engagement.community.domain.ReportStatus;
import com.synapse.engagement.community.domain.ReportTargetType;
import com.synapse.engagement.community.repository.ReportRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ModerationServiceTests {

    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final SharedContentService sharedContentService = mock(SharedContentService.class);
    private final GroupService groupService = mock(GroupService.class);
    private final CommunityNotificationPublisher publisher = mock(CommunityNotificationPublisher.class);
    private final ModerationService moderationService = new ModerationService(
            reportRepository, sharedContentService, groupService, publisher
    );

    @Test
    void approvedNotifiesReporterAndOwner() {
        // report: reporterId=10, SHARED_NOTE, targetId=99; owner=20
        var report = Report.create(10L, ReportTargetType.SHARED_NOTE, 99L, "spam");
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(sharedContentService.findOwnerId(ReportTargetType.SHARED_NOTE, 99L)).thenReturn(20L);

        moderationService.moderate(1L, new ReportModerateRequest(ReportStatus.APPROVED, "note"), "tenant-1");

        verify(publisher).publishModerationNotification(eq(10L), eq("tenant-1"), eq("REPORT_RESOLVED"), any(), any(), anyMap());
        verify(publisher).publishModerationNotification(eq(20L), eq("tenant-1"), eq("CONTENT_REMOVED"), any(), any(), anyMap());
        verifyNoMoreInteractions(publisher);
    }

    @Test
    void rejectedNotifiesReporterOnly() {
        var report = Report.create(10L, ReportTargetType.SHARED_NOTE, 99L, "spam");
        when(reportRepository.findById(2L)).thenReturn(Optional.of(report));

        moderationService.moderate(2L, new ReportModerateRequest(ReportStatus.REJECTED, "not a violation"), "tenant-1");

        verify(publisher).publishModerationNotification(eq(10L), eq("tenant-1"), eq("REPORT_REJECTED"), any(), any(), anyMap());
        verifyNoMoreInteractions(publisher);
    }

    @Test
    void approvedUserReportNotifiesReporterAndTargetUser() {
        // For USER target type, targetId IS the reported user
        var report = Report.create(10L, ReportTargetType.USER, 55L, "abuse");
        when(reportRepository.findById(3L)).thenReturn(Optional.of(report));

        moderationService.moderate(3L, new ReportModerateRequest(ReportStatus.APPROVED, "banned"), "tenant-1");

        verify(publisher).publishModerationNotification(eq(10L), eq("tenant-1"), eq("REPORT_RESOLVED"), any(), any(), anyMap());
        verify(publisher).publishModerationNotification(eq(55L), eq("tenant-1"), eq("CONTENT_REMOVED"), any(), any(), anyMap());
        verifyNoMoreInteractions(publisher);
    }
}
