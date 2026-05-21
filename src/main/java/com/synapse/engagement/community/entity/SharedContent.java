package com.synapse.engagement.community.entity;

import com.synapse.engagement.community.exception.SharedContentAccessDeniedException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "shared_contents")
public class SharedContent {

    private static final String TAG_DELIMITER = ",";

    /*
     * shared_contents는 Step 5의 공유 링크 메타데이터입니다.
     * 실제 덱/노트 본문을 복제하기보다, 어떤 콘텐츠가 어떤 shareToken으로 공개됐는지 기록합니다.
     */
    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 30)
    private ContentType contentType;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "share_token", nullable = false, unique = true, length = 64)
    private String shareToken;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String tags;

    @Column(name = "download_count", nullable = false)
    private int downloadCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected SharedContent() {
    }

    private SharedContent(
            UUID ownerId,
            ContentType contentType,
            UUID contentId,
            String shareToken,
            String title,
            String description,
            List<String> tags) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.ownerId = Objects.requireNonNull(ownerId);
        this.contentType = Objects.requireNonNull(contentType);
        this.contentId = Objects.requireNonNull(contentId);
        this.shareToken = requireText(shareToken, "shareToken");
        this.title = requireText(title, "title");
        this.description = normalize(description);
        this.tags = normalizeTags(tags);
        this.downloadCount = 0;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static SharedContent create(
            UUID ownerId,
            ContentType contentType,
            UUID contentId,
            String shareToken,
            String title,
            String description,
            List<String> tags) {
        /*
         * 새 공유 콘텐츠를 만들 때는 항상 이 팩토리 메서드를 사용합니다.
         * 생성자 안에서 UUID, 생성 시각, 기본 다운로드 수 같은 공통 초기값을 한 번에 세팅하기 위해서입니다.
         */
        return new SharedContent(ownerId, contentType, contentId, shareToken, title, description, tags);
    }

    public static SharedContent copyFor(UUID newOwnerId, SharedContent source, String shareToken) {
        /*
         * W2에는 learning-card 실제 복제 API를 연결하지 않습니다.
         * 대신 공유 메타데이터를 새 사용자 소유의 복사본으로 만들어 fork 흐름을 검증합니다.
         */
        return new SharedContent(
                newOwnerId,
                source.contentType,
                source.contentId,
                shareToken,
                source.title,
                source.description,
                source.tags());
    }

    public void incrementDownloadCount() {
        // fork가 발생했다는 최소한의 지표로 downloadCount를 올립니다.
        downloadCount++;
        updatedAt = LocalDateTime.now();
    }

    public void softDelete(UUID currentUserId) {
        // 공유 삭제는 소유자만 가능하며, 공개 링크를 더 이상 조회되지 않게 deletedAt을 채웁니다.
        requireOwner(currentUserId);
        deletedAt = LocalDateTime.now();
        updatedAt = deletedAt;
    }

    private void requireOwner(UUID currentUserId) {
        if (!ownerId.equals(currentUserId)) {
            throw new SharedContentAccessDeniedException(id);
        }
    }

    public UUID id() {
        return id;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public ContentType contentType() {
        return contentType;
    }

    public UUID contentId() {
        return contentId;
    }

    public String shareToken() {
        return shareToken;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public List<String> tags() {
        /*
         * DB에는 단순 문자열로 저장하지만 API 응답에서는 List<String>으로 보여줍니다.
         * Step 5 범위에서는 별도 tag 테이블 없이 검색 가능한 메타데이터로만 사용합니다.
         */
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(TAG_DELIMITER))
                .filter(tag -> !tag.isBlank())
                .toList();
    }

    public int downloadCount() {
        return downloadCount;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    private static String requireText(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return normalized;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private static String normalizeTags(List<String> values) {
        // 빈 태그는 버리고, 같은 태그가 중복 저장되지 않도록 distinct 처리합니다.
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join(TAG_DELIMITER, values.stream()
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .toList());
    }
}

