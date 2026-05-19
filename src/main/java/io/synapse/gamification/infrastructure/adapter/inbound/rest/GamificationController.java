package io.synapse.gamification.infrastructure.adapter.inbound.rest;

import io.synapse.gamification.application.usecase.GamificationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gamification")
// gamification API의 REST 진입점입니다. XP/배지 기능은 W2 이후 여기에서 확장됩니다.
class GamificationController {

    private final GamificationService gamificationService;

    GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }
}

