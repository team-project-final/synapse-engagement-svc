package com.synapse.engagement.community.domain;

import com.synapse.engagement.shared.ConflictException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportStatus status;

    @Column(name = "admin_note", length = 1000)
    private String adminNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected Report() {
    }

    private Report(Long reporterId, ReportTargetType targetType, Long targetId, String reason) {
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
    }

    public static Report create(Long reporterId, ReportTargetType targetType, Long targetId, String reason) {
        return new Report(reporterId, targetType, targetId, reason);
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void approve(String adminNote) {
        resolve(ReportStatus.APPROVED, adminNote);
    }

    public void reject(String adminNote) {
        resolve(ReportStatus.REJECTED, adminNote);
    }

    private void resolve(ReportStatus nextStatus, String adminNote) {
        // 신고는 한 번 처리되면 감사 기록으로 남아야 하므로 재처리 대신 409로 막는다.
        if (status != ReportStatus.PENDING) {
            throw new ConflictException("Report is already resolved: id=" + id);
        }
        this.status = nextStatus;
        this.adminNote = adminNote;
        this.resolvedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public ReportTargetType getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getReason() {
        return reason;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
