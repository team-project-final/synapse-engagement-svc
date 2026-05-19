package io.synapse.gamification.service;

import io.synapse.gamification.entity.EventType;
import io.synapse.gamification.entity.UserProfilesGamification;
import io.synapse.gamification.entity.XpEvent;
import io.synapse.gamification.repository.UserProfilesGamificationRepository;
import io.synapse.gamification.repository.XpEventRepository;
import io.synapse.gamification.dto.UserXpResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock
    private UserProfilesGamificationRepository profileRepository;

    @Mock
    private XpEventRepository xpEventRepository;

    @Mock
    private GamificationMapper gamificationMapper;

    private GamificationService gamificationService;

    @BeforeEach
    void setUp() {
        gamificationService = new GamificationService(profileRepository, xpEventRepository, gamificationMapper);
    }

    @Test
    @DisplayName("addXp_카드복습이벤트_shouldXP이벤트저장하고프로필갱신")
    void addXp_카드복습이벤트_shouldXP이벤트저장하고프로필갱신() {
        UUID userId = UUID.randomUUID();
        AddXpCommand command = command(userId, "event-1", "card-1");
        UserProfilesGamification profile = UserProfilesGamification.create(userId);
        UserXpResponse expected = new UserXpResponse(userId, 1, 10, 0, 0, "Novice", 100, List.of());

        given(xpEventRepository.existsByEventId("event-1")).willReturn(false);
        given(xpEventRepository.existsByUserIdAndEventTypeAndSourceId(userId, EventType.CARD_REVIEWED, "card-1"))
                .willReturn(false);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
        given(xpEventRepository.save(any(XpEvent.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(gamificationMapper.toResponse(profile)).willReturn(expected);

        UserXpResponse response = gamificationService.addXp(command);

        assertThat(response).isEqualTo(expected);
        assertThat(profile.totalXp()).isEqualTo(10);
        verify(xpEventRepository).save(any(XpEvent.class));
    }

    @Test
    @DisplayName("addXp_중복eventId_should저장하지않고현재프로필반환")
    void addXp_중복eventId_should저장하지않고현재프로필반환() {
        UUID userId = UUID.randomUUID();
        AddXpCommand command = command(userId, "event-1", "card-1");
        UserProfilesGamification profile = UserProfilesGamification.create(userId);
        UserXpResponse expected = new UserXpResponse(userId, 1, 0, 0, 0, "Novice", 100, List.of());

        given(xpEventRepository.existsByEventId("event-1")).willReturn(true);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
        given(gamificationMapper.toResponse(profile)).willReturn(expected);

        UserXpResponse response = gamificationService.addXp(command);

        assertThat(response).isEqualTo(expected);
        verify(xpEventRepository, never()).save(any(XpEvent.class));
    }

    private static AddXpCommand command(UUID userId, String eventId, String sourceId) {
        return new AddXpCommand(userId, EventType.CARD_REVIEWED, 10, sourceId, "CARD", eventId);
    }
}
