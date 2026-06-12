package com.synapse.engagement.gamification.service;

import com.synapse.engagement.gamification.entity.LevelDefinition;
import com.synapse.engagement.gamification.entity.UserProfilesGamification;
import com.synapse.engagement.gamification.repository.LevelDefinitionRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LevelServiceTest {

    @Mock
    private LevelDefinitionRepository levelRepo;

    private LevelService levelService;

    @BeforeEach
    void setUp() {
        levelService = new LevelService(levelRepo);
    }

    @Test
    @DisplayName("applyAndGet_XP100_should레벨2반환")
    void applyAndGet_XP100_should레벨2반환() {
        UserProfilesGamification profile = UserProfilesGamification.create(UUID.randomUUID());
        profile.addXp(100);

        // mock을 given() 바깥에서 먼저 생성합니다.
        LevelDefinition currentLevel = levelMock(2, 100, "Apprentice");
        LevelDefinition nextLevel = levelMock(3, 300, "Learner");

        given(levelRepo.findCurrentLevel(100)).willReturn(Optional.of(currentLevel));
        given(levelRepo.findByLevelNumber(3)).willReturn(Optional.of(nextLevel));

        LevelInfo info = levelService.applyAndGet(profile);

        assertThat(info.level()).isEqualTo(2);
        assertThat(info.title()).isEqualTo("Apprentice");
        assertThat(info.nextLevelXp()).isEqualTo(300);
        assertThat(profile.level()).isEqualTo(2);
    }

    @Test
    @DisplayName("applyAndGet_최고레벨_nextLevelXp는현재minXp반환")
    void applyAndGet_최고레벨_nextLevelXp는현재minXp반환() {
        UserProfilesGamification profile = UserProfilesGamification.create(UUID.randomUUID());
        profile.addXp(4500);

        LevelDefinition maxLevel = levelMock(10, 4500, "Grandmaster");

        given(levelRepo.findCurrentLevel(4500)).willReturn(Optional.of(maxLevel));
        given(levelRepo.findByLevelNumber(11)).willReturn(Optional.empty());

        LevelInfo info = levelService.applyAndGet(profile);

        assertThat(info.level()).isEqualTo(10);
        assertThat(info.nextLevelXp()).isEqualTo(4500);
    }

    @Test
    @DisplayName("applyAndGet_levelDefinitions없음_엔티티기존값유지")
    void applyAndGet_levelDefinitions없음_엔티티기존값유지() {
        UserProfilesGamification profile = UserProfilesGamification.create(UUID.randomUUID());

        given(levelRepo.findCurrentLevel(0)).willReturn(Optional.empty());

        LevelInfo info = levelService.applyAndGet(profile);

        assertThat(info.level()).isEqualTo(1);
        assertThat(info.title()).isEqualTo("Novice");
    }

    // lenient: 서비스 내부 로직에 따라 일부 stub이 호출 안 될 수 있습니다.
    private LevelDefinition levelMock(int levelNumber, int minXp, String title) {
        LevelDefinition ld = mock(LevelDefinition.class);
        lenient().when(ld.levelNumber()).thenReturn(levelNumber);
        lenient().when(ld.minXp()).thenReturn(minXp);
        lenient().when(ld.title()).thenReturn(title);
        return ld;
    }
}
