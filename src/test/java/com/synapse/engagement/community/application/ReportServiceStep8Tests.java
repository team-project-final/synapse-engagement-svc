package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.ReportCreateRequest;
import com.synapse.engagement.community.api.dto.ReportModerateRequest;
import com.synapse.engagement.community.domain.Report;
import com.synapse.engagement.community.domain.ReportStatus;
import com.synapse.engagement.community.domain.ReportTargetType;
import com.synapse.engagement.community.repository.ReportRepository;
import com.synapse.engagement.shared.ConflictException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceStep8Tests {

    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final SharedContentService sharedContentService = mock(SharedContentService.class);
    private final GroupService groupService = mock(GroupService.class);
    private final ReportService reportService = new ReportService(reportRepository, sharedContentService, groupService);
    private final ModerationService moderationService = new ModerationService(
            reportRepository,
            sharedContentService,
            groupService
    );

    @Test
    void createRejectsDuplicateReportForSameReporterAndTarget() {
        var request = new ReportCreateRequest(ReportTargetType.SHARED_DECK, 10L, "spam");
        when(reportRepository.existsByReporterIdAndTargetTypeAndTargetId(100L, ReportTargetType.SHARED_DECK, 10L))
                .thenReturn(true);

        assertThatThrownBy(() -> reportService.create(100L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(sharedContentService).requireReportableContent(ReportTargetType.SHARED_DECK, 10L);
    }

    @Test
    void approveSharedContentReportHidesTargetAndResolvesReport() {
        var report = Report.create(100L, ReportTargetType.SHARED_NOTE, 20L, "abuse");
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        var response = moderationService.moderate(
                1L,
                new ReportModerateRequest(ReportStatus.APPROVED, "hidden")
        );

        assertThat(response.status()).isEqualTo(ReportStatus.APPROVED);
        assertThat(response.adminNote()).isEqualTo("hidden");
        assertThat(response.resolvedAt()).isNotNull();
        verify(sharedContentService).hideReportedContent(ReportTargetType.SHARED_NOTE, 20L);
    }

    @Test
    void rejectReportDoesNotHideTarget() {
        var report = Report.create(100L, ReportTargetType.STUDY_GROUP, 30L, "not actually abuse");
        when(reportRepository.findById(2L)).thenReturn(Optional.of(report));

        var response = moderationService.moderate(
                2L,
                new ReportModerateRequest(ReportStatus.REJECTED, "not a violation")
        );

        assertThat(response.status()).isEqualTo(ReportStatus.REJECTED);
        assertThat(response.adminNote()).isEqualTo("not a violation");
        verify(reportRepository).findById(2L);
    }
}
