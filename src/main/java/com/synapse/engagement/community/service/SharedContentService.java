package com.synapse.engagement.community.service;

import com.synapse.engagement.community.dto.request.ShareContentRequest;
import com.synapse.engagement.community.dto.response.ShareTokenResponse;
import com.synapse.engagement.community.dto.response.SharedContentResponse;
import com.synapse.engagement.community.entity.ContentType;
import com.synapse.engagement.community.entity.SharedContent;
import com.synapse.engagement.community.exception.ShareTokenGenerationException;
import com.synapse.engagement.community.exception.SharedContentNotFoundException;
import com.synapse.engagement.community.repository.SharedContentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SharedContentService {

    /*
     * shareToken은 랜덤 생성이라 거의 충돌하지 않지만, DB UNIQUE 제약이 있으므로
     * 저장 전에 몇 번까지는 새 토큰을 다시 뽑아봅니다.
     */
    private static final int MAX_TOKEN_GENERATION_ATTEMPTS = 5;

    private final SharedContentRepository sharedContentRepository;
    private final ShareTokenGenerator tokenGenerator;
    private final SharedContentMapper mapper;

    SharedContentService(
            SharedContentRepository sharedContentRepository,
            ShareTokenGenerator tokenGenerator,
            SharedContentMapper mapper) {
        this.sharedContentRepository = sharedContentRepository;
        this.tokenGenerator = tokenGenerator;
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ShareTokenResponse share(UUID currentUserId, ShareContentRequest request) {
        /*
         * 공유 등록 흐름:
         * 1. 외부에 노출할 shareToken을 만든다.
         * 2. 콘텐츠 종류/원본 ID/제목/태그 같은 공유 메타데이터를 저장한다.
         * 3. 클라이언트가 바로 사용할 수 있는 share URL을 응답한다.
         */
        String shareToken = nextUniqueToken();
        SharedContent content = SharedContent.create(
                currentUserId,
                request.contentType(),
                request.contentId(),
                shareToken,
                request.title(),
                request.description(),
                request.tags());

        SharedContent savedContent = sharedContentRepository.save(content);
        return toTokenResponse(savedContent);
    }

    @Transactional(readOnly = true)
    public SharedContentResponse findByToken(String shareToken) {
        // shareToken은 공개 링크의 핵심 식별자입니다. 삭제된 공유 콘텐츠는 조회하지 않습니다.
        return mapper.toResponse(findActiveByToken(shareToken));
    }

    @Transactional(readOnly = true)
    public List<SharedContentResponse> search(String keyword, ContentType contentType, int size) {
        /*
         * keyword가 비어 있으면 최신 공유 콘텐츠 목록처럼 동작합니다.
         * contentType을 함께 주면 덱 또는 노트만 필터링할 수 있습니다.
         */
        String normalizedKeyword = normalizeKeyword(keyword);
        return sharedContentRepository.search(
                        normalizedKeyword,
                        contentType,
                        PageRequest.of(0, Math.min(size, 100)))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public SharedContentResponse fork(UUID currentUserId, String shareToken) {
        /*
         * fork 흐름:
         * - 원본 공유 콘텐츠를 token으로 찾는다.
         * - 원본의 downloadCount를 올린다.
         * - 현재 사용자 소유의 새 공유 콘텐츠 row를 만든다.
         *
         * 실제 learning-card 덱/노트 본문 복제는 W2 범위 밖이라 여기서는 메타데이터 복사까지만 처리합니다.
         */
        SharedContent source = findActiveByToken(shareToken);
        source.incrementDownloadCount();

        SharedContent copiedContent = SharedContent.copyFor(currentUserId, source, nextUniqueToken());
        return mapper.toResponse(sharedContentRepository.save(copiedContent));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(UUID currentUserId, UUID sharedContentId) {
        // 삭제는 소유자만 가능하고, 공개 링크를 무효화하기 위해 soft delete를 사용합니다.
        SharedContent content = sharedContentRepository.findByIdAndDeletedAtIsNull(sharedContentId)
                .orElseThrow(() -> new SharedContentNotFoundException(sharedContentId.toString()));
        content.softDelete(currentUserId);
    }

    private SharedContent findActiveByToken(String shareToken) {
        // token이 틀렸거나 이미 삭제된 공유 콘텐츠라면 동일하게 Not Found로 처리합니다.
        return sharedContentRepository.findByShareTokenAndDeletedAtIsNull(shareToken)
                .orElseThrow(() -> new SharedContentNotFoundException(shareToken));
    }

    private String nextUniqueToken() {
        /*
         * 토큰 충돌 가능성은 낮지만 0은 아닙니다.
         * 그래서 Repository로 이미 쓰인 토큰인지 확인하고, 충돌하면 새 토큰을 다시 생성합니다.
         */
        for (int attempt = 0; attempt < MAX_TOKEN_GENERATION_ATTEMPTS; attempt++) {
            String token = tokenGenerator.generate();
            if (!sharedContentRepository.existsByShareToken(token)) {
                return token;
            }
        }
        throw new ShareTokenGenerationException();
    }

    private static ShareTokenResponse toTokenResponse(SharedContent content) {
        // 프론트가 별도 조합 없이 바로 공유 링크를 표시할 수 있도록 URL도 함께 내려줍니다.
        String shareUrl = "/api/v1/community/share/" + content.shareToken();
        return new ShareTokenResponse(content.shareToken(), shareUrl);
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}

