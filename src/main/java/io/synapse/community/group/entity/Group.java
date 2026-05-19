package io.synapse.community.group.entity;

import io.synapse.community.group.exception.GroupAccessDeniedException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "CommunityGroup")
@Table(name = "groups")
// 학습 그룹 자체의 상태와 규칙을 가진 도메인 모델입니다.
public class Group {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected Group() {
    }

    private Group(UUID id, String name, String description, boolean isPublic, UUID ownerId) {
        LocalDateTime now = LocalDateTime.now();
        this.id = Objects.requireNonNull(id);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.createdAt = now;
        this.updatedAt = now;
        updateDetails(name, description, isPublic);
    }

    public static Group create(String name, String description, boolean isPublic, UUID ownerId) {
        // 새 그룹은 항상 새 UUID와 생성 시각을 갖고 시작합니다.
        return new Group(UUID.randomUUID(), name, description, isPublic, ownerId);
    }

    public void updateDetails(String name, String description, boolean isPublic) {
        // 그룹명/설명 검증은 서비스가 아니라 도메인 모델 안에서 처리합니다.
        this.name = validateName(name);
        this.description = normalizeDescription(description);
        this.isPublic = isPublic;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        // 실제 DB row는 지우지 않고 deletedAt만 기록해서 복구/감사 가능성을 남깁니다.
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = this.deletedAt;
    }

    public void requireOwner(UUID currentUserId) {
        // 수정/삭제 같은 소유자 전용 작업 전에 호출합니다.
        if (!ownerId.equals(currentUserId)) {
            throw new GroupAccessDeniedException(id);
        }
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
    }

    private static String validateName(String name) {
        String normalized = Objects.requireNonNull(name).trim();
        if (normalized.isBlank() || normalized.length() > 100) {
            throw new IllegalArgumentException("Group name must be 1 to 100 characters.");
        }
        return normalized;
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalized = description.trim();
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("Group description must be up to 500 characters.");
        }
        return normalized.isBlank() ? null : normalized;
    }
}
