package com.synapse.engagement.community.repository;

import com.synapse.engagement.community.domain.ContentType;
import com.synapse.engagement.community.domain.SharedContent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SharedContentRepositoryGroupScopeTests {

    @Autowired
    private SharedContentRepository repository;

    @Test
    void publicSearchExcludesGroupScopedContent() {
        repository.save(SharedContent.create(1L, ContentType.DECK, 100L, "tok-public-1", "Repo Alpha Public", null, "", null));
        repository.save(SharedContent.create(1L, ContentType.DECK, 101L, "tok-group-1", "Repo Alpha Group", null, "", 4001L));

        var found = repository.search("Repo Alpha", null);

        assertThat(found).extracting(SharedContent::getShareToken).containsExactly("tok-public-1");
    }

    @Test
    void searchByGroupIdReturnsOnlyThatGroup() {
        repository.save(SharedContent.create(1L, ContentType.NOTE, 200L, "tok-g-4002", "Repo Beta", null, "", 4002L));
        repository.save(SharedContent.create(1L, ContentType.NOTE, 201L, "tok-g-4003", "Repo Beta", null, "", 4003L));
        repository.save(SharedContent.create(1L, ContentType.NOTE, 202L, "tok-p-beta", "Repo Beta", null, "", null));

        var found = repository.searchByGroupId(4002L, null, null);

        assertThat(found).extracting(SharedContent::getShareToken).containsExactly("tok-g-4002");
    }

    @Test
    void searchByGroupIdAppliesKeywordAndContentType() {
        repository.save(SharedContent.create(1L, ContentType.DECK, 300L, "tok-g-match", "Repo Gamma Deck", null, "", 4004L));
        repository.save(SharedContent.create(1L, ContentType.NOTE, 301L, "tok-g-type", "Repo Gamma Note", null, "", 4004L));
        repository.save(SharedContent.create(1L, ContentType.DECK, 302L, "tok-g-word", "Repo Delta Deck", null, "", 4004L));

        var found = repository.searchByGroupId(4004L, "Gamma", ContentType.DECK);

        assertThat(found).extracting(SharedContent::getShareToken).containsExactly("tok-g-match");
    }
}
