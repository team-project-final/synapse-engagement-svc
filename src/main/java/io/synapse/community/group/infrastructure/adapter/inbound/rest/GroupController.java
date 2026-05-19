package io.synapse.community.group.infrastructure.adapter.inbound.rest;

import io.synapse.community.group.application.usecase.GroupService;
import io.synapse.community.group.infrastructure.adapter.inbound.rest.dto.GroupCreateRequest;
import io.synapse.community.group.infrastructure.adapter.inbound.rest.dto.GroupCursorResponse;
import io.synapse.community.group.infrastructure.adapter.inbound.rest.dto.GroupResponse;
import io.synapse.community.group.infrastructure.adapter.inbound.rest.dto.GroupUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups")
@Validated
@Tag(name = "Community Groups", description = "학습 그룹 CRUD API")
// 외부 HTTP 요청을 받아 GroupService usecase로 넘기는 REST 진입점입니다.
class GroupController {

    private final GroupService groupService;

    GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    ResponseEntity<GroupResponse> createGroup(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @Valid @RequestBody GroupCreateRequest request) {
        // W1 단계에서는 JWT 연동 전이라 X-User-Id 헤더를 현재 사용자로 사용합니다.
        GroupResponse response = groupService.createGroup(currentUserId, request);

        return ResponseEntity
                .created(URI.create("/api/v1/groups/" + response.id()))
                .body(response);
    }

    @GetMapping
    GroupCursorResponse listGroups(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
        // cursor가 없으면 첫 페이지, 있으면 다음 페이지를 조회합니다.
        return groupService.listGroups(cursor, size);
    }

    @GetMapping("/{groupId}")
    GroupResponse getGroup(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable("groupId") UUID groupId) {
        return groupService.getGroup(groupId);
    }

    @PutMapping("/{groupId}")
    GroupResponse updateGroup(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable("groupId") UUID groupId,
            @Valid @RequestBody GroupUpdateRequest request) {
        return groupService.updateGroup(currentUserId, groupId, request);
    }

    @DeleteMapping("/{groupId}")
    ResponseEntity<Void> deleteGroup(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable("groupId") UUID groupId) {
        groupService.deleteGroup(currentUserId, groupId);

        return ResponseEntity.noContent().build();
    }
}

