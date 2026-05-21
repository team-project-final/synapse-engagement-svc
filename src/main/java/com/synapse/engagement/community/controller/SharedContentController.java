package com.synapse.engagement.community.controller;

import com.synapse.engagement.community.dto.request.ShareContentRequest;
import com.synapse.engagement.community.dto.response.ShareTokenResponse;
import com.synapse.engagement.community.dto.response.SharedContentResponse;
import com.synapse.engagement.community.entity.ContentType;
import com.synapse.engagement.community.service.SharedContentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/community")
@Validated
public class SharedContentController {

    /*
     * Step 5의 REST 진입점입니다.
     *
     * 인증이 필요한 API:
     * - 공유 등록, fork, 삭제: 누가 작업했는지 알아야 하므로 X-User-Id 임시 헤더가 필요합니다.
     *
     * 공개 API:
     * - 토큰 조회, 검색: 공유 링크로 접근하는 사용자를 위해 인증 없이 열어둡니다.
     */
    private final SharedContentService sharedContentService;

    SharedContentController(SharedContentService sharedContentService) {
        this.sharedContentService = sharedContentService;
    }

    @PostMapping("/share")
    ResponseEntity<ShareTokenResponse> share(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @Valid @RequestBody ShareContentRequest request) {
        /*
         * 사용자가 자신의 덱/노트를 공유 상태로 등록합니다.
         * 성공하면 shareToken과, 그 토큰으로 접근할 수 있는 URL을 돌려줍니다.
         */
        ShareTokenResponse response = sharedContentService.share(currentUserId, request);

        return ResponseEntity
                .created(URI.create(response.shareUrl()))
                .body(response);
    }

    @GetMapping("/share/{token}")
    SharedContentResponse findByToken(@PathVariable("token") String token) {
        /*
         * 공유 링크를 받은 사용자가 토큰으로 콘텐츠 정보를 조회합니다.
         * 공개 공유 흐름이라 X-User-Id 헤더를 요구하지 않습니다.
         */
        return sharedContentService.findByToken(token);
    }

    @GetMapping("/search")
    List<SharedContentResponse> search(
            @RequestParam(name = "q", required = false) String keyword,
            @RequestParam(name = "contentType", required = false) ContentType contentType,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
        /*
         * 공개 공유 콘텐츠 검색입니다.
         * q가 없으면 최신 공유 콘텐츠를, contentType이 있으면 DECK/NOTE 중 하나만 조회합니다.
         */
        return sharedContentService.search(keyword, contentType, size);
    }

    @PostMapping("/share/{token}/fork")
    SharedContentResponse fork(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable("token") String token) {
        /*
         * 공유 콘텐츠를 내 소유의 복사본으로 만드는 흐름입니다.
         * W2에서는 실제 덱/노트 본문 복제 대신 공유 메타데이터 복사본을 만듭니다.
         */
        return sharedContentService.fork(currentUserId, token);
    }

    @DeleteMapping("/share/{id}")
    ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable("id") UUID id) {
        // 공유 등록자 본인만 삭제할 수 있고, 실제 row 삭제 대신 deletedAt을 기록합니다.
        sharedContentService.delete(currentUserId, id);
        return ResponseEntity.noContent().build();
    }
}

