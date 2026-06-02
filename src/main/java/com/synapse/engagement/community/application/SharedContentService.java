package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.ShareContentRequest;
import com.synapse.engagement.community.api.dto.ShareTokenResponse;
import com.synapse.engagement.community.api.dto.SharedContentResponse;
import com.synapse.engagement.community.domain.ContentType;
import com.synapse.engagement.community.domain.ReportTargetType;
import com.synapse.engagement.community.domain.SharedContent;
import com.synapse.engagement.community.repository.SharedContentRepository;
import com.synapse.engagement.shared.BadRequestException;
import com.synapse.engagement.shared.ForbiddenException;
import com.synapse.engagement.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class SharedContentService {
    private final SecureRandom secureRandom = new SecureRandom();
    private final SharedContentRepository sharedContentRepository;

    public SharedContentService(SharedContentRepository sharedContentRepository) {
        this.sharedContentRepository = sharedContentRepository;
    }

    @Transactional
    public ShareTokenResponse share(Long ownerId, ShareContentRequest request) {
        // shareToken은 URL에 노출되므로 예측 가능한 시퀀스 대신 SecureRandom 기반 토큰을 쓴다.
        var token = newToken();
        var content = sharedContentRepository.save(SharedContent.create(
                ownerId,
                request.contentType(),
                request.contentId(),
                token,
                request.title(),
                request.description(),
                joinTags(request.tags())
        ));
        return new ShareTokenResponse(content.getShareToken(), "/api/v1/community/share/" + content.getShareToken());
    }

    @Transactional(readOnly = true)
    public SharedContentResponse findByToken(String token) {
        return SharedContentResponse.from(findActiveByToken(token));
    }

    @Transactional(readOnly = true)
    public List<SharedContentResponse> search(String keyword, ContentType contentType) {
        // 빈 검색어는 null로 넘겨 repository가 "전체 검색 + 타입 필터"처럼 처리할 수 있게 한다.
        var normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return sharedContentRepository.search(normalizedKeyword, contentType).stream()
                .map(SharedContentResponse::from)
                .toList();
    }

    @Transactional
    public SharedContentResponse fork(Long userId, String token) {
        var source = findActiveByToken(token);
        // fork는 원본 조회 수를 올리고, 복사본에는 새 shareToken을 부여해 원본과 URL을 분리한다.
        source.incrementDownloadCount();
        var forked = sharedContentRepository.save(source.fork(userId, newToken()));
        return SharedContentResponse.from(forked);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        var content = sharedContentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Shared content not found: id=" + id));
        if (!content.isOwnedBy(userId)) {
            throw new ForbiddenException("Only the owner can delete shared content");
        }
        content.delete();
    }

    @Transactional(readOnly = true)
    public void requireReportableContent(ReportTargetType targetType, Long sharedContentId) {
        findActiveContentForReport(targetType, sharedContentId);
    }

    @Transactional
    public void hideReportedContent(ReportTargetType targetType, Long sharedContentId) {
        // 모더레이션 승인은 소유자 권한과 별개로 관리자 결정이므로 owner check 없이 soft delete만 수행한다.
        findActiveContentForReport(targetType, sharedContentId).delete();
    }

    private SharedContent findActiveByToken(String token) {
        return sharedContentRepository.findByShareTokenAndDeletedAtIsNull(token)
                .orElseThrow(() -> new NotFoundException("Shared content not found"));
    }

    private SharedContent findActiveContentForReport(ReportTargetType targetType, Long sharedContentId) {
        var contentType = switch (targetType) {
            case SHARED_DECK -> ContentType.DECK;
            case SHARED_NOTE -> ContentType.NOTE;
            default -> throw new BadRequestException("Unsupported shared content report target: " + targetType);
        };
        return sharedContentRepository.findByIdAndContentTypeAndDeletedAtIsNull(sharedContentId, contentType)
                .orElseThrow(() -> new NotFoundException("Shared content not found: id=" + sharedContentId));
    }

    private String newToken() {
        // 24 random bytes는 URL-safe base64로 약 32자 토큰이 되어 공유 URL에 쓰기 좋다.
        var bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }
}
