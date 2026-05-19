package io.synapse.gamification.service;

import io.synapse.gamification.entity.UserProfilesGamification;
import io.synapse.gamification.entity.XpEvent;
import io.synapse.gamification.repository.UserProfilesGamificationRepository;
import io.synapse.gamification.repository.XpEventRepository;
import io.synapse.gamification.dto.UserXpResponse;
import io.synapse.gamification.dto.XpEventResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamificationService {

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
        if (isDuplicate(command)) {
            return getProfile(command.userId());
        }

        UserProfilesGamification profile = profileRepository.findByUserId(command.userId())
                .orElseGet(() -> profileRepository.save(UserProfilesGamification.create(command.userId())));
        XpEvent event = XpEvent.create(
                command.userId(),
                command.eventType(),
                command.xpAmount(),
                command.sourceId(),
                command.sourceType(),
                command.eventId());

        xpEventRepository.save(event);
        profile.addXp(command.xpAmount());

        return gamificationMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public UserXpResponse getProfile(UUID userId) {
        UserProfilesGamification profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfilesGamification.create(userId));

        return gamificationMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<XpEventResponse> getXpHistory(UUID userId, int size) {
        return gamificationMapper.toEventResponses(xpEventRepository.findByUserIdOrderByCreatedAtDesc(userId, size));
    }

    private boolean isDuplicate(AddXpCommand command) {
        return xpEventRepository.existsByEventId(command.eventId())
                || xpEventRepository.existsByUserIdAndEventTypeAndSourceId(
                        command.userId(),
                        command.eventType(),
                        command.sourceId());
    }
}
