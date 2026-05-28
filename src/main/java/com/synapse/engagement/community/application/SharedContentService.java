package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.ShareContentRequest;
import com.synapse.engagement.community.api.dto.ShareTokenResponse;
import com.synapse.engagement.community.api.dto.SharedContentResponse;
import com.synapse.engagement.community.domain.ContentType;
import com.synapse.engagement.community.domain.SharedContent;
import com.synapse.engagement.community.repository.SharedContentRepository;
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
        var normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return sharedContentRepository.search(normalizedKeyword, contentType).stream()
                .map(SharedContentResponse::from)
                .toList();
    }

    @Transactional
    public SharedContentResponse fork(Long userId, String token) {
        var source = findActiveByToken(token);
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

    private SharedContent findActiveByToken(String token) {
        return sharedContentRepository.findByShareTokenAndDeletedAtIsNull(token)
                .orElseThrow(() -> new NotFoundException("Shared content not found"));
    }

    private String newToken() {
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
