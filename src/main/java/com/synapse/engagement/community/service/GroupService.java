package com.synapse.engagement.community.service;

import com.synapse.engagement.community.entity.Group;
import com.synapse.engagement.community.dto.request.GroupCreateRequest;
import com.synapse.engagement.community.dto.response.GroupCursorResponse;
import com.synapse.engagement.community.dto.response.GroupResponse;
import com.synapse.engagement.community.dto.request.GroupUpdateRequest;
import com.synapse.engagement.community.exception.GroupLimitExceededException;
import com.synapse.engagement.community.exception.GroupNotFoundException;
import com.synapse.engagement.community.repository.GroupRepository;
import com.synapse.engagement.community.entity.GroupMember;
import com.synapse.engagement.community.repository.GroupMemberRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService {

    // 한 사용자가 만들 수 있는 활성 그룹 수 제한입니다.
    private static final int MAX_GROUPS_PER_OWNER = 10;

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMapper groupMapper;

    GroupService(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            GroupMapper groupMapper) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMapper = groupMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public GroupResponse createGroup(UUID currentUserId, GroupCreateRequest request) {
        // 그룹 생성 전, 사용자별 생성 제한을 먼저 확인합니다.
        checkGroupCreateLimit(currentUserId);

        // 요청 DTO 값을 도메인 객체로 바꾼 뒤 저장합니다.
        Group newGroup = Group.create(
                request.name(),
                request.description(),
                request.isPublic(),
                currentUserId);
        Group savedGroup = groupRepository.save(newGroup);

        // 그룹 생성자는 자동으로 OWNER 멤버가 됩니다.
        addOwnerAsFirstMember(savedGroup, currentUserId);
        return groupMapper.toResponse(savedGroup);
    }

    @Transactional(readOnly = true)
    public GroupCursorResponse listGroups(String cursor, int size) {
        // size보다 1개 더 조회해서 다음 페이지가 있는지 판단합니다.
        List<Group> groups = findGroups(cursor, size);
        boolean hasNext = hasNextPage(groups, size);
        List<Group> pageItems = currentPageItems(groups, size, hasNext);

        return new GroupCursorResponse(
                pageItems.stream().map(groupMapper::toResponse).toList(),
                nextCursor(pageItems, hasNext),
                hasNext);
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(UUID groupId) {
        // 삭제되지 않은 그룹만 조회합니다.
        return groupMapper.toResponse(findActiveGroup(groupId));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public GroupResponse updateGroup(UUID currentUserId, UUID groupId, GroupUpdateRequest request) {
        Group group = findActiveGroup(groupId);
        // 그룹 소유자만 그룹 정보를 수정할 수 있습니다.
        group.requireOwner(currentUserId);
        group.updateDetails(request.name(), request.description(), request.isPublic());

        return groupMapper.toResponse(group);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteGroup(UUID currentUserId, UUID groupId) {
        Group group = findActiveGroup(groupId);
        // 그룹 소유자만 삭제할 수 있고, 실제 삭제 대신 deletedAt을 기록합니다.
        group.requireOwner(currentUserId);
        group.softDelete();
    }

    private Group findActiveGroup(UUID groupId) {
        // deletedAt이 없는 그룹만 정상 그룹으로 취급합니다.
        return groupRepository.findActiveById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private void checkGroupCreateLimit(UUID ownerId) {
        long activeGroupCount = groupRepository.countActiveGroupsByOwnerId(ownerId);
        if (activeGroupCount >= MAX_GROUPS_PER_OWNER) {
            throw new GroupLimitExceededException(MAX_GROUPS_PER_OWNER);
        }
    }

    private void addOwnerAsFirstMember(Group group, UUID ownerId) {
        groupMemberRepository.save(GroupMember.owner(group.id(), ownerId));
    }

    private List<Group> findGroups(String cursor, int size) {
        // cursor가 없으면 첫 페이지, 있으면 cursor 이후의 다음 페이지를 조회합니다.
        GroupCursor decodedCursor = GroupCursor.decode(cursor);
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        if (decodedCursor == null) {
            return groupRepository.findFirstVisibleGroups(pageRequest);
        }
        return groupRepository.findVisibleGroupsAfter(decodedCursor.createdAt(), decodedCursor.id(), pageRequest);
    }

    private static boolean hasNextPage(List<Group> groups, int size) {
        return groups.size() > size;
    }

    private static List<Group> currentPageItems(List<Group> groups, int size, boolean hasNext) {
        return hasNext ? groups.subList(0, size) : groups;
    }

    private static String nextCursor(List<Group> pageItems, boolean hasNext) {
        if (!hasNext) {
            return null;
        }
        return GroupCursor.encode(pageItems.get(pageItems.size() - 1));
    }
}

