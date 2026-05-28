package com.synapse.engagement.community.api;

import com.synapse.engagement.community.api.dto.GroupCreateRequest;
import com.synapse.engagement.community.api.dto.GroupResponse;
import com.synapse.engagement.community.api.dto.GroupUpdateRequest;
import com.synapse.engagement.community.application.GroupService;
import com.synapse.engagement.shared.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/community/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody GroupCreateRequest request
    ) {
        return groupService.create(CurrentUser.require(jwt), request);
    }

    @GetMapping
    public List<GroupResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return groupService.findAll(page, size);
    }

    @GetMapping("/{groupId}")
    public GroupResponse findById(@PathVariable Long groupId) {
        return groupService.findById(groupId);
    }

    @PutMapping("/{groupId}")
    public GroupResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @Valid @RequestBody GroupUpdateRequest request
    ) {
        return groupService.update(groupId, CurrentUser.require(jwt), request);
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId
    ) {
        groupService.delete(groupId, CurrentUser.require(jwt));
    }
}
