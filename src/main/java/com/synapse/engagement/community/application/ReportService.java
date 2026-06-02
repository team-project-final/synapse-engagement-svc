package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.ReportCreateRequest;
import com.synapse.engagement.community.api.dto.ReportResponse;
import com.synapse.engagement.community.domain.Report;
import com.synapse.engagement.community.domain.ReportStatus;
import com.synapse.engagement.community.domain.ReportTargetType;
import com.synapse.engagement.community.repository.ReportRepository;
import com.synapse.engagement.shared.BadRequestException;
import com.synapse.engagement.shared.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final SharedContentService sharedContentService;
    private final GroupService groupService;

    public ReportService(
            ReportRepository reportRepository,
            SharedContentService sharedContentService,
            GroupService groupService
    ) {
        this.reportRepository = reportRepository;
        this.sharedContentService = sharedContentService;
        this.groupService = groupService;
    }

    @Transactional
    public ReportResponse create(Long reporterId, ReportCreateRequest request) {
        // 신고 대상이 실제로 존재하는지 먼저 확인해 잘못된 targetId가 moderation queue에 쌓이지 않게 한다.
        requireReportableTarget(request.targetType(), request.targetId());
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporterId,
                request.targetType(),
                request.targetId()
        )) {
            throw new ConflictException("Report already exists for this target");
        }

        var report = reportRepository.save(Report.create(
                reporterId,
                request.targetType(),
                request.targetId(),
                request.reason()
        ));
        return ReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> findByStatus(ReportStatus status) {
        return reportRepository.findByStatusOrderByCreatedAtAsc(status).stream()
                .map(ReportResponse::from)
                .toList();
    }

    private void requireReportableTarget(ReportTargetType targetType, Long targetId) {
        switch (targetType) {
            case SHARED_DECK, SHARED_NOTE -> sharedContentService.requireReportableContent(targetType, targetId);
            case STUDY_GROUP -> groupService.requireReportableGroup(targetId);
            case USER -> requirePositiveUserTarget(targetId);
        }
    }

    private void requirePositiveUserTarget(Long targetId) {
        // engagement-svc에는 user 테이블이 없으므로 여기서는 ID 형태만 검증하고, 실제 사용자 존재성은 platform/auth 영역에 맡긴다.
        if (targetId == null || targetId <= 0) {
            throw new BadRequestException("User report target id must be positive");
        }
    }
}
