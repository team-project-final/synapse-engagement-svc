package com.synapse.engagement.community.service;

import com.synapse.engagement.community.dto.request.ShareContentRequest;
import com.synapse.engagement.community.dto.response.ShareTokenResponse;
import com.synapse.engagement.community.dto.response.SharedContentResponse;
import com.synapse.engagement.community.entity.ContentType;
import com.synapse.engagement.community.entity.SharedContent;
import com.synapse.engagement.community.exception.SharedContentNotFoundException;
import com.synapse.engagement.community.repository.SharedContentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SharedContentServiceTest {

    @Mock
    private SharedContentRepository sharedContentRepository;

    @Mock
    private ShareTokenGenerator tokenGenerator;

    private SharedContentService sharedContentService;

    @BeforeEach
    void setUp() {
        sharedContentService = new SharedContentService(
                sharedContentRepository,
                tokenGenerator,
                new SharedContentMapper());
    }

    @Test
    @DisplayName("share_정상요청_should공유토큰반환")
    void share_정상요청_should공유토큰반환() {
        UUID ownerId = UUID.randomUUID();
        ShareContentRequest request = request();
        given(tokenGenerator.generate()).willReturn("share-token");
        given(sharedContentRepository.existsByShareToken("share-token")).willReturn(false);
        given(sharedContentRepository.save(any(SharedContent.class))).willAnswer(invocation -> invocation.getArgument(0));

        ShareTokenResponse response = sharedContentService.share(ownerId, request);

        assertThat(response.shareToken()).isEqualTo("share-token");
        assertThat(response.shareUrl()).isEqualTo("/api/v1/community/share/share-token");
        verify(sharedContentRepository).save(any(SharedContent.class));
    }

    @Test
    @DisplayName("findByToken_없는토큰_shouldThrowNotFound")
    void findByToken_없는토큰_shouldThrowNotFound() {
        given(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("missing")).willReturn(Optional.empty());

        assertThatThrownBy(() -> sharedContentService.findByToken("missing"))
                .isInstanceOf(SharedContentNotFoundException.class);
    }

    @Test
    @DisplayName("fork_공유토큰_should복사본생성하고다운로드증가")
    void fork_공유토큰_should복사본생성하고다운로드증가() {
        UUID ownerId = UUID.randomUUID();
        SharedContent source = SharedContent.create(
                UUID.randomUUID(),
                ContentType.DECK,
                UUID.randomUUID(),
                "source-token",
                "Deck",
                "Description",
                List.of("spring"));

        given(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("source-token")).willReturn(Optional.of(source));
        given(tokenGenerator.generate()).willReturn("copy-token");
        given(sharedContentRepository.existsByShareToken("copy-token")).willReturn(false);
        given(sharedContentRepository.save(any(SharedContent.class))).willAnswer(invocation -> invocation.getArgument(0));

        SharedContentResponse response = sharedContentService.fork(ownerId, "source-token");

        assertThat(source.downloadCount()).isEqualTo(1);
        assertThat(response.ownerId()).isEqualTo(ownerId);
        assertThat(response.shareToken()).isEqualTo("copy-token");
    }

    private static ShareContentRequest request() {
        return new ShareContentRequest(
                ContentType.DECK,
                UUID.randomUUID(),
                "Spring Deck",
                "Study deck",
                List.of("spring", "java"));
    }
}

