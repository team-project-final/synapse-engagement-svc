package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.GroupCreateRequest;
import com.synapse.engagement.community.api.dto.GroupResponse;
import com.synapse.engagement.community.api.dto.GroupUpdateRequest;
import com.synapse.engagement.community.domain.Group;
import com.synapse.engagement.community.domain.GroupMember;
import com.synapse.engagement.community.repository.GroupMemberRepository;
import com.synapse.engagement.community.repository.GroupRepository;
import com.synapse.engagement.shared.ForbiddenException;
import com.synapse.engagement.shared.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {
    private static final int MAX_GROUPS_PER_USER = 10;

    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository memberRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public GroupResponse create(Long ownerId, GroupCreateRequest request) {
        if (groupRepository.countByOwnerIdAndDeletedAtIsNull(ownerId) >= MAX_GROUPS_PER_USER) {
            throw new ForbiddenException("A user can create up to 10 groups");
        }
        var group = groupRepository.save(Group.create(
                request.name(),
                request.description(),
                request.isPublic(),
                ownerId
        ));
        memberRepository.save(GroupMember.owner(group));
        return GroupResponse.from(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> findAll(int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return groupRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable)
                .stream()
                .map(GroupResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse findById(Long id) {
        return GroupResponse.from(findActiveGroup(id));
    }

    @Transactional
    public GroupResponse update(Long groupId, Long userId, GroupUpdateRequest request) {
        var group = findActiveGroup(groupId);
        requireOwner(group, userId);
        group.update(request.name(), request.description(), request.isPublic());
        return GroupResponse.from(group);
    }

    @Transactional
    public void delete(Long groupId, Long userId) {
        var group = findActiveGroup(groupId);
        requireOwner(group, userId);
        group.delete();
    }

    Group findActiveGroup(Long groupId) {
        return groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found: id=" + groupId));
    }

    private void requireOwner(Group group, Long userId) {
        if (!group.isOwnedBy(userId)) {
            throw new ForbiddenException("Only the group owner can change this group");
        }
    }
}
