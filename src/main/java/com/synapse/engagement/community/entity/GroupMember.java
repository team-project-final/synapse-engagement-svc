package com.synapse.engagement.community.entity;

import com.synapse.engagement.community.exception.GroupMemberPermissionDeniedException;
import com.synapse.engagement.community.exception.OwnerCannotLeaveException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "group_members")
// 사용자가 어떤 그룹에 어떤 역할/상태로 속해 있는지 표현하는 도메인 모델입니다.
public class GroupMember {

    // 강퇴된 멤버는 이 기간 동안 재가입할 수 없습니다.
    private static final int REJOIN_BLOCK_DAYS = 7;

    @Id
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupMemberStatus status;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "kicked_at")
    private LocalDateTime kickedAt;

    protected GroupMember() {
    }

    private GroupMember(UUID groupId, UUID userId, GroupMemberRole role, GroupMemberStatus status) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.groupId = Objects.requireNonNull(groupId);
        this.userId = Objects.requireNonNull(userId);
        this.role = Objects.requireNonNull(role);
        this.status = Objects.requireNonNull(status);
        this.joinedAt = status == GroupMemberStatus.ACTIVE ? now : null;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static GroupMember owner(UUID groupId, UUID userId) {
        // 그룹 생성자는 처음부터 ACTIVE OWNER로 등록됩니다.
        return new GroupMember(groupId, userId, GroupMemberRole.OWNER, GroupMemberStatus.ACTIVE);
    }

    public static GroupMember joined(UUID groupId, UUID userId, boolean publicGroup) {
        // 공개 그룹은 즉시 가입, 비공개 그룹은 승인 대기 상태로 시작합니다.
        return new GroupMember(
                groupId,
                userId,
                GroupMemberRole.MEMBER,
                publicGroup ? GroupMemberStatus.ACTIVE : GroupMemberStatus.PENDING);
    }

    public void approve() {
        // 이미 승인된 멤버에게 approve가 다시 와도 상태를 바꾸지 않습니다.
        if (status != GroupMemberStatus.PENDING) {
            return;
        }

        status = GroupMemberStatus.ACTIVE;
        joinedAt = LocalDateTime.now();
        updatedAt = joinedAt;
    }

    public void leave(UUID currentUserId) {
        // 탈퇴는 본인만 요청할 수 있고, OWNER는 바로 탈퇴할 수 없습니다.
        if (!userId.equals(currentUserId)) {
            throw new GroupMemberPermissionDeniedException(groupId);
        }
        if (role == GroupMemberRole.OWNER) {
            throw new OwnerCannotLeaveException(groupId);
        }

        status = GroupMemberStatus.KICKED;
        kickedAt = null;
        updatedAt = LocalDateTime.now();
    }

    public void kick() {
        // OWNER는 강퇴 대상이 될 수 없습니다.
        if (role == GroupMemberRole.OWNER) {
            throw new OwnerCannotLeaveException(groupId);
        }

        status = GroupMemberStatus.KICKED;
        kickedAt = LocalDateTime.now();
        updatedAt = kickedAt;
    }

    public void reactivate(boolean publicGroup) {
        // 재가입 시 공개 여부에 따라 ACTIVE 또는 PENDING으로 되돌립니다.
        status = publicGroup ? GroupMemberStatus.ACTIVE : GroupMemberStatus.PENDING;
        joinedAt = publicGroup ? LocalDateTime.now() : null;
        kickedAt = null;
        updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == GroupMemberStatus.ACTIVE;
    }

    public boolean blocksRejoin(LocalDateTime now) {
        // 강퇴 시각이 있고 7일이 지나지 않았으면 재가입을 막습니다.
        return status == GroupMemberStatus.KICKED
                && kickedAt != null
                && kickedAt.plusDays(REJOIN_BLOCK_DAYS).isAfter(now);
    }

    public boolean canManageMembers() {
        return isActive() && (role == GroupMemberRole.OWNER || role == GroupMemberRole.ADMIN);
    }

    public boolean isOwner() {
        return isActive() && role == GroupMemberRole.OWNER;
    }

    public UUID id() {
        return id;
    }

    public UUID groupId() {
        return groupId;
    }

    public UUID userId() {
        return userId;
    }

    public GroupMemberRole role() {
        return role;
    }

    public GroupMemberStatus status() {
        return status;
    }

    public LocalDateTime joinedAt() {
        return joinedAt;
    }
}

