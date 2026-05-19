package io.synapse.community.group.member.infrastructure.adapter.inbound.rest;

import io.synapse.community.group.member.application.usecase.MemberService;
import io.synapse.community.group.member.infrastructure.adapter.inbound.rest.dto.MemberInviteRequest;
import io.synapse.community.group.member.infrastructure.adapter.inbound.rest.dto.MemberResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/members")
@Validated
@Tag(name = "Community Group Members", description = "학습 그룹 멤버 관리 API")
// 외부 HTTP 요청을 받아 MemberService usecase로 넘기는 REST 진입점입니다.
public class MemberController {

    private final MemberService memberService;

    MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/invite")
    MemberResponse invite(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable("groupId") UUID groupId,
            @Valid @RequestBody MemberInviteRequest request) {
        // OWNER가 특정 사용자를 그룹에 초대합니다.
        return memberService.invite(currentUserId, groupId, request);
    }

    @PostMapping("/join")
    MemberResponse join(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable("groupId") UUID groupId) {
        // 현재 사용자가 직접 그룹 가입을 요청합니다.
        return memberService.join(currentUserId, groupId);
    }

    @PutMapping("/{memberId}/approve")
    MemberResponse approve(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable("groupId") UUID groupId,
            @PathVariable("memberId") UUID memberId) {
        // PENDING 멤버를 ACTIVE로 승인합니다.
        return memberService.approve(currentUserId, groupId, memberId);
    }

    @DeleteMapping("/{memberId}")
    ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable("groupId") UUID groupId,
            @PathVariable("memberId") UUID memberId) {
        memberService.delete(currentUserId, groupId, memberId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    List<MemberResponse> list(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable("groupId") UUID groupId) {
        return memberService.list(currentUserId, groupId);
    }
}

