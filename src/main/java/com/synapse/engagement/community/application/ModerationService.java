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
import java.util.Optional;

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
            // 승인 처리는 신고 상태 변경과 대상 숨김을 같은 트랜잭션에 묶어 moderation 결과가 엇갈리지 않게 한다.
            hideTarget(report);
            report.approve(request.adminNote());
            ReportResponse response = ReportResponse.from(report);
            // best-effort: 알림 발행 실패가 모더레이션 트랜잭션을 깨지 않도록 감싼다.
            try {
                notifyReporter(report, tenantId, "REPORT_RESOLVED", "신고가 처리되었습니다", "신고하신 콘텐츠가 제재되었습니다.");
                resolveReportedUserId(report).ifPresent(ownerId ->
                        publisher.publishModerationNotification(ownerId, tenantId, "CONTENT_REMOVED",
                                "콘텐츠가 제재되었습니다", "신고 검토 결과 회원님의 콘텐츠가 제재되었습니다.",
                                Map.of("reportId", String.valueOf(report.getId()))));
            } catch (Exception e) {
                log.warn("알림 발행 실패 (best-effort, 모더레이션 트랜잭션은 유지): reportId={}", reportId, e);
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

    private Optional<Long> resolveReportedUserId(Report report) {
        return switch (report.getTargetType()) {
            case USER -> Optional.of(report.getTargetId()); // targetId == 피신고자
            case SHARED_DECK, SHARED_NOTE -> Optional.ofNullable(sharedContentService.findOwnerId(report.getTargetType(), report.getTargetId()));
            case STUDY_GROUP -> Optional.ofNullable(groupService.findOwnerId(report.getTargetId()));
        };
    }
}
