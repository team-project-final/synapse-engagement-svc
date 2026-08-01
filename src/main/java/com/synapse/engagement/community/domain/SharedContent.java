package com.synapse.engagement.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "shared_contents")
public class SharedContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "share_token", nullable = false, unique = true, length = 80)
    private String shareToken;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String tags;

    @Column(name = "download_count", nullable = false)
    private long downloadCount;

    @Column(name = "source_share_id")
    private Long sourceShareId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected SharedContent() {
    }

    private SharedContent(
            Long ownerId,
            ContentType contentType,
            Long contentId,
            String shareToken,
            String title,
            String description,
            String tags,
            Long sourceShareId,
            Long groupId
    ) {
        this.ownerId = ownerId;
        this.contentType = contentType;
        this.contentId = contentId;
        this.shareToken = shareToken;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.sourceShareId = sourceShareId;
        this.groupId = groupId;
    }

    public static SharedContent create(
            Long ownerId,
            ContentType contentType,
            Long contentId,
            String shareToken,
            String title,
            String description,
            String tags,
            Long groupId
    ) {
        return new SharedContent(ownerId, contentType, contentId, shareToken, title, description, tags, null, groupId);
    }

    // fork는 "내 것으로 가져오기"이므로 그룹 스코프를 승계하지 않는다(마지막 인자 null).
    // 승계하면 그룹을 나간 뒤 자기 사본에 접근하지 못하는 모순이 생긴다.
    public SharedContent fork(Long newOwnerId, String newToken) {
        return new SharedContent(newOwnerId, contentType, contentId, newToken, title, description, tags, id, null);
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void incrementDownloadCount() {
        downloadCount += 1;
    }

    public void delete() {
        deletedAt = Instant.now();
    }

    public boolean isOwnedBy(Long userId) {
        return ownerId.equals(userId);
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public Long getContentId() {
        return contentId;
    }

    public String getShareToken() {
        return shareToken;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTags() {
        return tags;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public Long getSourceShareId() {
        return sourceShareId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public boolean isGroupScoped() {
        return groupId != null;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
