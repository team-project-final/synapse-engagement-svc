package com.synapse.engagement.community.api;

import com.synapse.engagement.community.api.dto.ShareContentRequest;
import com.synapse.engagement.community.api.dto.ShareTokenResponse;
import com.synapse.engagement.community.api.dto.SharedContentResponse;
import com.synapse.engagement.community.application.SharedContentService;
import com.synapse.engagement.community.domain.ContentType;
import com.synapse.engagement.shared.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/community")
public class SharedContentController {
    private final SharedContentService sharedContentService;

    public SharedContentController(SharedContentService sharedContentService) {
        this.sharedContentService = sharedContentService;
    }

    @PostMapping("/share")
    @ResponseStatus(HttpStatus.CREATED)
    public ShareTokenResponse share(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ShareContentRequest request
    ) {
        return sharedContentService.share(CurrentUser.require(jwt), request);
    }

    @GetMapping("/share/{token}")
    public SharedContentResponse findByToken(@PathVariable String token) {
        return sharedContentService.findByToken(token);
    }

    @GetMapping("/search")
    public List<SharedContentResponse> search(
            @RequestParam(required = false, name = "q") String keyword,
            @RequestParam(required = false) ContentType contentType
    ) {
        return sharedContentService.search(keyword, contentType);
    }

    @PostMapping("/share/{token}/fork")
    @ResponseStatus(HttpStatus.CREATED)
    public SharedContentResponse fork(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String token
    ) {
        return sharedContentService.fork(CurrentUser.require(jwt), token);
    }

    @DeleteMapping("/share/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        sharedContentService.delete(CurrentUser.require(jwt), id);
    }
}
