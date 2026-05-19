package io.synapse.community.group.application.usecase;

import io.synapse.community.group.domain.model.Group;
import io.synapse.community.group.infrastructure.adapter.inbound.rest.dto.GroupCreateRequest;
import io.synapse.community.group.infrastructure.adapter.inbound.rest.dto.GroupResponse;
import io.synapse.community.group.infrastructure.adapter.inbound.rest.dto.GroupUpdateRequest;
import io.synapse.community.group.domain.model.exception.GroupAccessDeniedException;
import io.synapse.community.group.domain.model.exception.GroupLimitExceededException;
import io.synapse.community.group.domain.port.outbound.GroupRepositoryPort;
import io.synapse.community.group.member.domain.port.outbound.GroupMemberRepositoryPort;
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
class GroupServiceTest {

    @Mock
    private GroupRepositoryPort groupRepository;

    @Mock
    private GroupMemberRepositoryPort memberRepository;

    @Mock
    private GroupMapper groupMapper;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupService(groupRepository, memberRepository, groupMapper);
    }

    @Test
    @DisplayName("createGroup_사용자그룹10개이상_should예외")
    void createGroup_사용자그룹10개이상_should예외() {
        UUID ownerId = UUID.randomUUID();
        given(groupRepository.countActiveGroupsByOwnerId(ownerId)).willReturn(10L);

        assertThatThrownBy(() -> groupService.createGroup(
                ownerId,
                new GroupCreateRequest("Too Many", null, true)))
                .isInstanceOf(GroupLimitExceededException.class);
    }

    @Test
    @DisplayName("createGroup_정상요청_should저장후응답")
    void createGroup_정상요청_should저장후응답() {
        UUID ownerId = UUID.randomUUID();
        GroupResponse expected = new GroupResponse(null, "Spring Study", null, true, ownerId, null);
        given(groupRepository.countActiveGroupsByOwnerId(ownerId)).willReturn(0L);
        given(groupRepository.save(any(Group.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(groupMapper.toResponse(any(Group.class))).willReturn(expected);

        GroupResponse response = groupService.createGroup(
                ownerId,
                new GroupCreateRequest("Spring Study", null, true));

        assertThat(response).isEqualTo(expected);
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    @DisplayName("updateGroup_비소유자_should예외")
    void updateGroup_비소유자_should예외() {
        UUID ownerId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        Group group = Group.create("Owner Only", null, true, ownerId);
        given(groupRepository.findActiveById(groupId)).willReturn(Optional.of(group));

        assertThatThrownBy(() -> groupService.updateGroup(
                UUID.randomUUID(),
                groupId,
                new GroupUpdateRequest("Blocked", null, true)))
                .isInstanceOf(GroupAccessDeniedException.class);
    }
}

