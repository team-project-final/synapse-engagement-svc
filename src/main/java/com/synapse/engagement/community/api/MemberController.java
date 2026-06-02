package com.synapse.engagement.community.api;

import com.synapse.engagement.community.api.dto.InviteDecisionResponse;
import com.synapse.engagement.community.api.dto.JoinRequestDecisionRequest;
import com.synapse.engagement.community.api.dto.JoinRequestResponse;
import com.synapse.engagement.community.api.dto.MemberInviteRequest;
import com.synapse.engagement.community.api.dto.MemberResponse;
import com.synapse.engagement.community.application.MemberService;
import com.synapse.engagement.shared.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/community/groups/{groupId}")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/members/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteDecisionResponse invite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @Valid @RequestBody MemberInviteRequest request
    ) {
        // 초대자는 JWT 사용자이며, Service에서 OWNER/ADMIN인지 확인한 뒤 초대를 생성한다.
        return memberService.invite(groupId, CurrentUser.require(jwt), request);
    }

    @PostMapping("/members/join")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse join(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId
    ) {
        // 공개 그룹은 즉시 ACTIVE, 비공개 그룹은 PENDING으로 들어가는 정책은 도메인 객체가 결정한다.
        return memberService.join(groupId, CurrentUser.require(jwt));
    }

    @PutMapping("/members/{memberId}/approve")
    public MemberResponse approve(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long memberId
    ) {
        return memberService.approve(groupId, CurrentUser.require(jwt), memberId);
    }

    @DeleteMapping("/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long memberId
    ) {
        memberService.remove(groupId, CurrentUser.require(jwt), memberId);
    }

    @GetMapping("/members")
    public List<MemberResponse> list(@PathVariable Long groupId) {
        return memberService.list(groupId);
    }

    @PostMapping("/invite/{token}/accept")
    public InviteDecisionResponse acceptInvite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable String token
    ) {
        return memberService.acceptInvite(groupId, CurrentUser.require(jwt), token);
    }

    @PostMapping("/invite/{token}/decline")
    public InviteDecisionResponse declineInvite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable String token
    ) {
        return memberService.declineInvite(groupId, CurrentUser.require(jwt), token);
    }

    @GetMapping("/join-requests")
    public List<JoinRequestResponse> joinRequests(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId
    ) {
        return memberService.listJoinRequests(groupId, CurrentUser.require(jwt));
    }

    @PatchMapping("/join-requests/{userId}")
    public MemberResponse decideJoinRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @Valid @RequestBody JoinRequestDecisionRequest request
    ) {
        // 가입 요청 승인/거절은 멤버 row의 상태 전이이므로 Service에서 상태와 권한을 함께 검증한다.
        return memberService.decideJoinRequest(groupId, CurrentUser.require(jwt), userId, request);
    }
}
