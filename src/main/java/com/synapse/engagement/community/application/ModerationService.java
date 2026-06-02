package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.ReportModerateRequest;
import com.synapse.engagement.community.api.dto.ReportResponse;
import com.synapse.engagement.community.domain.Report;
import com.synapse.engagement.community.domain.ReportStatus;
import com.synapse.engagement.community.repository.ReportRepository;
import com.synapse.engagement.shared.BadRequestException;
import com.synapse.engagement.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModerationService {
    private final ReportRepository reportRepository;
    private final SharedContentService sharedContentService;
    private final GroupService groupService;

    public ModerationService(
            ReportRepository reportRepository,
            SharedContentService sharedContentService,
            GroupService groupService
    ) {
        this.reportRepository = reportRepository;
        this.sharedContentService = sharedContentService;
        this.groupService = groupService;
    }

    @Transactional
    public ReportResponse moderate(Long reportId, ReportModerateRequest request) {
        var report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found: id=" + reportId));

        if (request.status() == ReportStatus.APPROVED) {
            // 승인 처리는 신고 상태 변경과 대상 숨김을 같은 트랜잭션에 묶어 moderation 결과가 엇갈리지 않게 한다.
            hideTarget(report);
            report.approve(request.adminNote());
            return ReportResponse.from(report);
        }
        if (request.status() == ReportStatus.REJECTED) {
            report.reject(request.adminNote());
            return ReportResponse.from(report);
        }
        throw new BadRequestException("Moderation status must be APPROVED or REJECTED");
    }

    private void hideTarget(Report report) {
        switch (report.getTargetType()) {
            case SHARED_DECK, SHARED_NOTE -> sharedContentService.hideReportedContent(
                    report.getTargetType(),
                    report.getTargetId()
            );
            case STUDY_GROUP -> groupService.hideReportedGroup(report.getTargetId());
            case USER -> {
                // 사용자 제재는 engagement-svc가 소유한 데이터가 아니므로 신고만 승인하고 실제 계정 조치는 platform/auth에서 처리한다.
            }
        }
    }
}
