package com.synapse.engagement.gamification.controller;

import com.synapse.engagement.gamification.dto.response.UserXpResponse;
import com.synapse.engagement.gamification.dto.response.XpEventResponse;
import com.synapse.engagement.gamification.service.GamificationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gamification")
@Validated
public class GamificationController {

    /*
     * Step 4의 조회용 REST API입니다.
     * platform-svc JWT 검증 연동 전까지는 X-User-Id 임시 헤더로 현재 사용자를 식별합니다.
     * XP 적립 자체는 여러 입력 경로에서 재사용할 수 있도록 Service의 AddXpCommand로 분리되어 있습니다.
     */
    private final GamificationService gamificationService;

    GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/profile")
    UserXpResponse getProfile(@RequestHeader("X-User-Id") UUID currentUserId) {
        /*
         * "내 프로필" 조회이므로 클라이언트가 userId를 path/query로 보내지 않습니다.
         * 지금은 임시로 X-User-Id 헤더를 쓰지만, 나중에는 인증 principal에서 같은 값을 꺼내면 됩니다.
         */
        return gamificationService.getProfile(currentUserId);
    }

    @GetMapping("/xp/history")
    List<XpEventResponse> getXpHistory(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
        /*
         * XP 이력은 많아질 수 있으므로 한 번에 가져올 개수를 제한합니다.
         * @Min/@Max는 잘못된 size 요청을 Controller 진입 단계에서 막아줍니다.
         */
        return gamificationService.getXpHistory(currentUserId, size);
    }
}

