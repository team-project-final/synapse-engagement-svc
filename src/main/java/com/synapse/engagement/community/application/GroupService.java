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
        // 그룹 생성 수 제한은 비용/스팸 방어용 정책이다. 삭제된 그룹은 카운트하지 않는다.
        if (groupRepository.countByOwnerIdAndDeletedAtIsNull(ownerId) >= MAX_GROUPS_PER_USER) {
            throw new ForbiddenException("A user can create up to 10 groups");
        }
        var group = groupRepository.save(Group.create(
                request.name(),
                request.description(),
                request.isPublic(),
                ownerId
        ));
        // 그룹 생성자는 즉시 OWNER 멤버십을 가진다. 이후 멤버 관리 권한은 이 멤버십에서 나온다.
        memberRepository.save(GroupMember.owner(group));
        return GroupResponse.from(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> findAll(int page, int size) {
        // page/size는 API 경계에서 보정해 과도한 목록 조회를 막는다.
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
        // 실제 row 삭제 대신 soft delete로 숨겨 복구/감사 가능성을 남긴다.
        group.delete();
    }

    @Transactional(readOnly = true)
    public void requireReportableGroup(Long groupId) {
        findActiveGroup(groupId);
    }

    @Transactional
    public void hideReportedGroup(Long groupId) {
        // 관리자 모더레이션 승인은 일반 소유자 삭제와 다른 경로라 owner check 없이 soft delete한다.
        findActiveGroup(groupId).delete();
    }

    Group findActiveGroup(Long groupId) {
        return groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found: id=" + groupId));
    }

    private void requireOwner(Group group, Long userId) {
        // 소유자만 그룹 메타데이터를 바꿀 수 있고, ADMIN/MEMBER 권한은 멤버 관리로 범위를 좁힌다.
        if (!group.isOwnedBy(userId)) {
            throw new ForbiddenException("Only the group owner can change this group");
        }
    }
}
