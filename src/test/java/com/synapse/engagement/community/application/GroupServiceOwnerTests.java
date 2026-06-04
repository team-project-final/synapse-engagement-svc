package com.synapse.engagement.community.application;

import com.synapse.engagement.community.domain.Group;
import com.synapse.engagement.community.repository.GroupMemberRepository;
import com.synapse.engagement.community.repository.GroupRepository;
import com.synapse.engagement.shared.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupServiceOwnerTests {

    private final GroupRepository groupRepository = mock(GroupRepository.class);
    private final GroupMemberRepository memberRepository = mock(GroupMemberRepository.class);
    private final GroupService service = new GroupService(groupRepository, memberRepository);

    @Test
    void findOwnerIdReturnsGroupOwner() {
        var group = Group.create("Test Group", "desc", true, 77L);
        when(groupRepository.findById(5L)).thenReturn(Optional.of(group));

        Long ownerId = service.findOwnerId(5L);

        assertThat(ownerId).isEqualTo(77L);
    }

    @Test
    void findOwnerIdThrowsWhenGroupNotFound() {
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findOwnerId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }
}
