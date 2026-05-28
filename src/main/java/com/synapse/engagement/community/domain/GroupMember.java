package com.synapse.engagement.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "group_members")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "kicked_at")
    private Instant kickedAt;

    @Column(name = "invite_token", length = 80)
    private String inviteToken;

    @Column(name = "invite_expires_at")
    private Instant inviteExpiresAt;

    protected GroupMember() {
    }

    private GroupMember(Group group, Long userId, MemberRole role, MemberStatus status) {
        this.group = group;
        this.userId = userId;
        this.role = role;
        this.status = status;
    }

    public static GroupMember owner(Group group) {
        return new GroupMember(group, group.getOwnerId(), MemberRole.OWNER, MemberStatus.ACTIVE);
    }

    public static GroupMember invited(Group group, Long userId, String inviteToken, Instant inviteExpiresAt) {
        GroupMember member = new GroupMember(group, userId, MemberRole.MEMBER, MemberStatus.INVITED);
        member.inviteToken = inviteToken;
        member.inviteExpiresAt = inviteExpiresAt;
        return member;
    }

    public static GroupMember joined(Group group, Long userId, boolean activeImmediately) {
        return new GroupMember(group, userId, MemberRole.MEMBER,
                activeImmediately ? MemberStatus.ACTIVE : MemberStatus.PENDING);
    }

    @PrePersist
    void onCreate() {
        if (status == MemberStatus.ACTIVE && joinedAt == null) {
            joinedAt = Instant.now();
        }
    }

    public void approve() {
        this.status = MemberStatus.ACTIVE;
        this.joinedAt = Instant.now();
    }

    public void reject() {
        this.status = MemberStatus.REJECTED;
    }

    public void acceptInvite() {
        this.status = MemberStatus.ACTIVE;
        this.joinedAt = Instant.now();
        this.inviteToken = null;
        this.inviteExpiresAt = null;
    }

    public void declineInvite() {
        this.status = MemberStatus.DECLINED;
        this.inviteToken = null;
        this.inviteExpiresAt = null;
    }

    public void kick() {
        this.status = MemberStatus.KICKED;
        this.kickedAt = Instant.now();
        this.inviteToken = null;
        this.inviteExpiresAt = null;
    }

    public void reactivate(boolean activeImmediately) {
        this.status = activeImmediately ? MemberStatus.ACTIVE : MemberStatus.PENDING;
        this.kickedAt = null;
        this.inviteToken = null;
        this.inviteExpiresAt = null;
        if (activeImmediately) {
            this.joinedAt = Instant.now();
        }
    }

    public void reinvite(String inviteToken, Instant inviteExpiresAt) {
        this.status = MemberStatus.INVITED;
        this.kickedAt = null;
        this.inviteToken = inviteToken;
        this.inviteExpiresAt = inviteExpiresAt;
    }

    public void requestToJoin(boolean activeImmediately) {
        this.status = activeImmediately ? MemberStatus.ACTIVE : MemberStatus.PENDING;
        this.kickedAt = null;
        this.inviteToken = null;
        this.inviteExpiresAt = null;
        if (activeImmediately) {
            this.joinedAt = Instant.now();
        }
    }

    public boolean canModerate() {
        return status == MemberStatus.ACTIVE && (role == MemberRole.OWNER || role == MemberRole.ADMIN);
    }

    public boolean isOwner() {
        return role == MemberRole.OWNER;
    }

    public Long getId() {
        return id;
    }

    public Group getGroup() {
        return group;
    }

    public Long getUserId() {
        return userId;
    }

    public MemberRole getRole() {
        return role;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getKickedAt() {
        return kickedAt;
    }

    public String getInviteToken() {
        return inviteToken;
    }

    public Instant getInviteExpiresAt() {
        return inviteExpiresAt;
    }
}
