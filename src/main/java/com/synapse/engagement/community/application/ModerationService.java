package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.ReportModerateRequest;
import com.synapse.engagement.community.api.dto.ReportResponse;
import com.synapse.engagement.community.application.event.CommunityNotificationPublisher;
import com.synapse.engagement.community.domain.Report;
import com.synapse.engagement.community.domain.ReportStatus;
import com.synapse.engagement.community.repository.ReportRepository;
import com.synapse.engagement.shared.BadRequestException;
import com.synapse.engagement.shared.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ModerationService {
    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);

    private final ReportRepository reportRepository;
    private final SharedContentService sharedContentService;
    private final GroupService groupService;
    private final CommunityNotificationPublisher publisher;

    public ModerationService(
            ReportRepository reportRepository,
            SharedContentService sharedContentService,
            GroupService groupService,
            CommunityNotificationPublisher publisher
    ) {
        this.reportRepository = reportRepository;
        this.sharedContentService = sharedContentService;
        this.groupService = groupService;
        this.publisher = publisher;
    }

    @Transactional
    public ReportResponse moderate(Long reportId, ReportModerateRequest request, String tenantId) {
        var report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found: id=" + reportId));

        if (request.status() == ReportStatus.APPROVED) {
            // owner를 먼저 확인한다: hideTarget이 soft-delete를 수행하면 findByIdAndDeletedAtIsNull이
            // empty를 반환해 NotFoundException → 트랜잭션 롤백으로 이어지는 버그를 방지하기 위해
            // 콘텐츠가 아직 살아 있는 시점에 owner를 조회한다.
            java.util.Optional<Long> ownerIdOpt = resolveReportedUserId(report); // BEFORE hideTarget (content still present)
            hideTarget(report);
            report.approve(request.adminNote());
            ReportResponse response = ReportResponse.from(report);
            // best-effort: 알림 발행 실패가 모더레이션 트랜잭션을 깨지 않도록 감싼다.
            try {
                notifyReporter(report, tenantId, "REPORT_RESOLVED", "신고가 처리되었습니다", "신고하신 콘텐츠가 제재되었습니다.");
                ownerIdOpt.ifPresent(ownerId -> publisher.publishModerationNotification(
                        ownerId, tenantId, "CONTENT_REMOVED", "콘텐츠가 제재되었습니다",
                        "신고 검토 결과 회원님의 콘텐츠가 제재되었습니다.",
                        java.util.Map.of("reportId", String.valueOf(report.getId()))));
            } catch (Exception e) {
                log.warn("Moderation notification publish failed (reportId={}): {}", report.getId(), e.getMessage());
            }
            return response;
        }
        if (request.status() == ReportStatus.REJECTED) {
            report.reject(request.adminNote());
            ReportResponse response = ReportResponse.from(report);
            // best-effort: 알림 발행 실패가 모더레이션 트랜잭션을 깨지 않도록 감싼다.
            try {
                notifyReporter(report, tenantId, "REPORT_REJECTED", "신고가 기각되었습니다", "신고 검토 결과 조치가 이루어지지 않았습니다.");
            } catch (Exception e) {
                log.warn("알림 발행 실패 (best-effort, 모더레이션 트랜잭션은 유지): reportId={}", reportId, e);
            }
            return response;
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

    private void notifyReporter(Report report, String tenantId, String type, String title, String body) {
        publisher.publishModerationNotification(report.getReporterId(), tenantId, type, title, body,
                Map.of("reportId", String.valueOf(report.getId()), "targetType", report.getTargetType().name()));
    }

    private java.util.Optional<Long> resolveReportedUserId(Report report) {
        try {
            return switch (report.getTargetType()) {
                case USER -> java.util.Optional.of(report.getTargetId()); // targetId == 피신고자
                case SHARED_DECK, SHARED_NOTE -> java.util.Optional.of(
                        sharedContentService.findOwnerId(report.getTargetType(), report.getTargetId()));
                case STUDY_GROUP -> java.util.Optional.of(groupService.findOwnerId(report.getTargetId()));
            };
        } catch (NotFoundException e) {
            log.warn("Reported owner not found, skipping owner notification (reportId={}): {}", report.getId(), e.getMessage());
            return java.util.Optional.empty();
        }
    }
}
