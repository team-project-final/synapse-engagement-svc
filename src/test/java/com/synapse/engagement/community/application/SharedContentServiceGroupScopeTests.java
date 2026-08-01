package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.ShareContentRequest;
import com.synapse.engagement.community.domain.ContentType;
import com.synapse.engagement.community.domain.Group;
import com.synapse.engagement.community.domain.GroupMember;
import com.synapse.engagement.community.domain.MemberStatus;
import com.synapse.engagement.community.domain.SharedContent;
import com.synapse.engagement.community.repository.GroupMemberRepository;
import com.synapse.engagement.community.repository.GroupRepository;
import com.synapse.engagement.community.repository.SharedContentRepository;
import com.synapse.engagement.shared.ForbiddenException;
import com.synapse.engagement.shared.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SharedContentServiceGroupScopeTests {

    private static final Long GROUP_ID = 500L;
    private static final Long MEMBER_ID = 11L;
    private static final Long OUTSIDER_ID = 12L;

    private final SharedContentRepository sharedContentRepository = mock(SharedContentRepository.class);
    private final GroupRepository groupRepository = mock(GroupRepository.class);
    private final GroupMemberRepository groupMemberRepository = mock(GroupMemberRepository.class);
    private final SharedContentService service =
            new SharedContentService(sharedContentRepository, groupRepository, groupMemberRepository);

    @Test
    void shareToGroupSucceedsForActiveMember() {
        givenGroupExists();
        givenMembership(MEMBER_ID, MemberStatus.ACTIVE);
        when(sharedContentRepository.save(any(SharedContent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.share(MEMBER_ID, request(GROUP_ID));

        assertThat(response.shareToken()).isNotBlank();
    }

    @Test
    void shareToGroupRejectsPendingMember() {
        givenGroupExists();
        givenMembership(MEMBER_ID, MemberStatus.PENDING);

        assertThatThrownBy(() -> service.share(MEMBER_ID, request(GROUP_ID)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shareToGroupRejectsNonMember() {
        givenGroupExists();
        when(groupMemberRepository.findByGroupIdAndUserId(GROUP_ID, OUTSIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.share(OUTSIDER_ID, request(GROUP_ID)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shareToMissingGroupIsNotFound() {
        when(groupRepository.findByIdAndDeletedAtIsNull(GROUP_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.share(MEMBER_ID, request(GROUP_ID)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void searchInGroupRejectsNonMember() {
        givenGroupExists();
        when(groupMemberRepository.findByGroupIdAndUserId(GROUP_ID, OUTSIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.searchInGroup(GROUP_ID, OUTSIDER_ID, null, null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void searchInGroupReturnsContentForActiveMember() {
        givenGroupExists();
        givenMembership(MEMBER_ID, MemberStatus.ACTIVE);
        when(sharedContentRepository.searchByGroupId(GROUP_ID, null, null))
                .thenReturn(List.of(groupContent()));

        var found = service.searchInGroup(GROUP_ID, MEMBER_ID, null, null);

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().groupId()).isEqualTo(GROUP_ID);
    }

    @Test
    void findByTokenHidesGroupContentFromAnonymous() {
        when(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("tok"))
                .thenReturn(Optional.of(groupContent()));

        assertThatThrownBy(() -> service.findByToken("tok", null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByTokenHidesGroupContentFromNonMember() {
        when(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("tok"))
                .thenReturn(Optional.of(groupContent()));
        when(groupMemberRepository.findByGroupIdAndUserId(GROUP_ID, OUTSIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByToken("tok", OUTSIDER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByTokenAllowsPublicContentForAnonymous() {
        when(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("pub"))
                .thenReturn(Optional.of(SharedContent.create(1L, ContentType.NOTE, 9L, "pub", "Public", null, "", null)));

        var response = service.findByToken("pub", null);

        assertThat(response.groupId()).isNull();
    }

    @Test
    void forkOfGroupContentByNonMemberIsNotFound() {
        when(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("tok"))
                .thenReturn(Optional.of(groupContent()));
        when(groupMemberRepository.findByGroupIdAndUserId(GROUP_ID, OUTSIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.fork(OUTSIDER_ID, "tok"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void forkOfGroupContentByMemberProducesPersonalCopy() {
        when(sharedContentRepository.findByShareTokenAndDeletedAtIsNull("tok"))
                .thenReturn(Optional.of(groupContent()));
        givenMembership(MEMBER_ID, MemberStatus.ACTIVE);
        when(sharedContentRepository.save(any(SharedContent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var forked = service.fork(MEMBER_ID, "tok");

        assertThat(forked.groupId()).isNull();
        assertThat(forked.ownerId()).isEqualTo(MEMBER_ID);
    }

    private ShareContentRequest request(Long groupId) {
        return new ShareContentRequest(ContentType.DECK, 1L, "title", null, List.of(), groupId);
    }

    private SharedContent groupContent() {
        return SharedContent.create(1L, ContentType.DECK, 9L, "tok", "Group content", null, "", GROUP_ID);
    }

    private void givenGroupExists() {
        when(groupRepository.findByIdAndDeletedAtIsNull(GROUP_ID)).thenReturn(Optional.of(mock(Group.class)));
    }

    private void givenMembership(Long userId, MemberStatus status) {
        var member = mock(GroupMember.class);
        when(member.getStatus()).thenReturn(status);
        when(groupMemberRepository.findByGroupIdAndUserId(GROUP_ID, userId)).thenReturn(Optional.of(member));
    }
}
