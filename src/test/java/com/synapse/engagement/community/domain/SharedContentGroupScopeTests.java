package com.synapse.engagement.community.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SharedContentGroupScopeTests {

    @Test
    void createWithoutGroupIsNotGroupScoped() {
        var content = SharedContent.create(1L, ContentType.NOTE, 10L, "tok", "title", null, "", null);

        assertThat(content.getGroupId()).isNull();
        assertThat(content.isGroupScoped()).isFalse();
    }

    @Test
    void createWithGroupIsGroupScoped() {
        var content = SharedContent.create(1L, ContentType.DECK, 10L, "tok", "title", null, "", 77L);

        assertThat(content.getGroupId()).isEqualTo(77L);
        assertThat(content.isGroupScoped()).isTrue();
    }

    @Test
    void forkDoesNotInheritGroupScope() {
        var source = SharedContent.create(1L, ContentType.DECK, 10L, "tok", "title", "desc", "a,b", 77L);

        var forked = source.fork(2L, "tok2");

        assertThat(forked.getGroupId()).isNull();
        assertThat(forked.isGroupScoped()).isFalse();
        assertThat(forked.getOwnerId()).isEqualTo(2L);
        assertThat(forked.getShareToken()).isEqualTo("tok2");
    }
}
