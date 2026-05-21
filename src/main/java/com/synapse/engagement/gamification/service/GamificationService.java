package com.synapse.engagement.gamification.service;

import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import com.synapse.engagement.gamification.entity.XpEvent;
import com.synapse.engagement.gamification.repository.UserProfilesGamificationRepository;
import com.synapse.engagement.gamification.repository.XpEventRepository;
import com.synapse.engagement.gamification.dto.response.UserXpResponse;
import com.synapse.engagement.gamification.dto.response.XpEventResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamificationService {

    /*
     * Step 4의 중심 서비스입니다.
     *
     * 역할은 크게 3가지입니다.
     * 1. XP를 적립할 때 xp_events에 "무슨 활동으로 XP가 생겼는지" 이력을 남긴다.
     * 2. user_profiles_gamification에 "사용자의 현재 총 XP/레벨"을 누적해서 저장한다.
     * 3. 같은 이벤트가 두 번 들어와도 XP가 중복 적립되지 않도록 막는다.
     */
    private final UserProfilesGamificationRepository profileRepository;
    private final XpEventRepository xpEventRepository;
    private final GamificationMapper gamificationMapper;

    GamificationService(
            UserProfilesGamificationRepository profileRepository,
            XpEventRepository xpEventRepository,
            GamificationMapper gamificationMapper) {
        this.profileRepository = profileRepository;
        this.xpEventRepository = xpEventRepository;
        this.gamificationMapper = gamificationMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserXpResponse addXp(AddXpCommand command) {
        /*
         * 멱등성(idempotency) 확인:
         * 같은 이벤트가 재시도나 중복 요청으로 여러 번 들어와도 결과는 한 번 처리한 것과 같아야 합니다.
         * 이미 처리한 이벤트라면 새 XP 이벤트를 저장하지 않고 현재 프로필만 반환합니다.
         */
        if (isDuplicate(command)) {
            return getProfile(command.userId());
        }

        /*
         * 최초로 XP를 받는 사용자는 아직 프로필 row가 없을 수 있습니다.
         * 그래서 없으면 기본 프로필(level 1, totalXp 0)을 먼저 만들고, 그 위에 XP를 더합니다.
         */
        UserProfilesGamification profile = profileRepository.findByUserId(command.userId())
                .orElseGet(() -> profileRepository.save(UserProfilesGamification.create(command.userId())));

        /*
         * XpEvent는 "누적 결과"가 아니라 "이번에 발생한 한 번의 XP 적립 사건"입니다.
         * 예: 카드 복습(card-1)을 완료해서 10 XP를 받았다.
         */
        XpEvent event = XpEvent.create(
                command.userId(),
                command.eventType(),
                command.xpAmount(),
                command.sourceId(),
                command.sourceType(),
                command.eventId());

        /*
         * 이벤트 이력 저장 후 프로필 누적값을 갱신합니다.
         * 두 작업은 같은 트랜잭션 안에서 실행되므로, 중간에 실패하면 둘 다 롤백됩니다.
         */
        xpEventRepository.save(event);
        profile.addXp(command.xpAmount());

        return gamificationMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public UserXpResponse getProfile(UUID userId) {
        /*
         * 조회 API에서는 DB에 새 프로필을 저장하지 않습니다.
         * 아직 XP 활동이 없는 사용자도 화면에서 기본값을 볼 수 있도록 임시 기본 프로필을 만들어 응답합니다.
         */
        UserProfilesGamification profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfilesGamification.create(userId));

        return gamificationMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<XpEventResponse> getXpHistory(UUID userId, int size) {
        /*
         * 최신 XP 적립 내역부터 size개만 조회합니다.
         * 컨트롤러와 Repository 양쪽에서 최대 크기를 제한해 너무 큰 조회를 막습니다.
         */
        return gamificationMapper.toEventResponses(xpEventRepository.findByUserIdOrderByCreatedAtDesc(userId, size));
    }

    private boolean isDuplicate(AddXpCommand command) {
        /*
         * 중복 판단 기준은 2개입니다.
         * 1. eventId: 외부/내부 이벤트 자체의 고유 ID가 이미 처리됐는가?
         * 2. userId + eventType + sourceId: 같은 사용자가 같은 원본 활동으로 이미 XP를 받았는가?
         */
        return xpEventRepository.existsByEventId(command.eventId())
                || xpEventRepository.existsByUserIdAndEventTypeAndSourceId(
                        command.userId(),
                        command.eventType(),
                        command.sourceId());
    }
}

