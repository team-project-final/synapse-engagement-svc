package com.synapse.engagement.community.application;

import com.synapse.engagement.community.domain.ContentType;
import com.synapse.engagement.community.domain.ReportTargetType;
import com.synapse.engagement.community.domain.SharedContent;
import com.synapse.engagement.community.repository.SharedContentRepository;
import com.synapse.engagement.shared.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SharedContentServiceOwnerTests {

    private final SharedContentRepository repo = mock(SharedContentRepository.class);
    private final SharedContentService service = new SharedContentService(repo);

    @Test
    void findOwnerIdReturnsOwnerOfSharedContent() {
        var content = SharedContent.create(99L, ContentType.NOTE, 1L, "tok", "title", null, "");
        when(repo.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(content));

        Long ownerId = service.findOwnerId(ReportTargetType.SHARED_NOTE, 10L);

        assertThat(ownerId).isEqualTo(99L);
    }

    @Test
    void findOwnerIdThrowsWhenContentNotFound() {
        when(repo.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findOwnerId(ReportTargetType.SHARED_DECK, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }
}
