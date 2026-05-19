package io.synapse.gamification.api;

import io.synapse.gamification.service.GamificationService;
import io.synapse.gamification.dto.UserXpResponse;
import io.synapse.gamification.dto.XpEventResponse;
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
class GamificationController {

    private final GamificationService gamificationService;

    GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/profile")
    UserXpResponse getProfile(@RequestHeader("X-User-Id") UUID currentUserId) {
        return gamificationService.getProfile(currentUserId);
    }

    @GetMapping("/xp/history")
    List<XpEventResponse> getXpHistory(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
        return gamificationService.getXpHistory(currentUserId, size);
    }
}
