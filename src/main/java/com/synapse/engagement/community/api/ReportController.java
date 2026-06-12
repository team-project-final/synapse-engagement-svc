package com.synapse.engagement.community.api;

import com.synapse.engagement.community.api.dto.ReportCreateRequest;
import com.synapse.engagement.community.api.dto.ReportModerateRequest;
import com.synapse.engagement.community.api.dto.ReportResponse;
import com.synapse.engagement.community.application.ModerationService;
import com.synapse.engagement.community.application.ReportService;
import com.synapse.engagement.community.domain.ReportStatus;
import com.synapse.engagement.shared.CurrentTenant;
import com.synapse.engagement.shared.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ReportController {
    private final ReportService reportService;
    private final ModerationService moderationService;

    public ReportController(ReportService reportService, ModerationService moderationService) {
        this.reportService = reportService;
        this.moderationService = moderationService;
    }

    @PostMapping("/community/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        // reporterId는 응답에 노출하지 않고 JWT subject에서만 저장해 신고 대상자가 신고자를 알 수 없게 한다.
        return reportService.create(CurrentUser.require(jwt), request);
    }

    @GetMapping("/admin/reports")
    public List<ReportResponse> findReports(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "PENDING") ReportStatus status
    ) {
        // 관리자 목록 API는 URL만 보호하는 대신 JWT role claim까지 확인해 403 테스트 가능성을 명확히 한다.
        CurrentUser.requireAdmin(jwt);
        return reportService.findByStatus(status);
    }

    @PatchMapping("/admin/reports/{reportId}")
    public ReportResponse moderate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long reportId,
            @Valid @RequestBody ReportModerateRequest request
    ) {
        CurrentUser.requireAdmin(jwt);
        String tenantId = CurrentTenant.resolve(jwt);
        return moderationService.moderate(reportId, request, tenantId);
    }
}
