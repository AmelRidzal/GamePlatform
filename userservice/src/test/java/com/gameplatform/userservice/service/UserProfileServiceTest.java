package com.gameplatform.userservice.service;

import com.gameplatform.userservice.dto.CreateProfileRequest;
import com.gameplatform.userservice.dto.UserProfileDto;
import com.gameplatform.userservice.entity.UserProfile;
import com.gameplatform.userservice.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository repository;

    @InjectMocks
    private UserProfileService service;

    private UserProfile existingProfile;

    @BeforeEach
    void setUp() {
        existingProfile = new UserProfile();
        existingProfile.setUserId(1L);
        existingProfile.setUsername("testuser");
        existingProfile.setDisplayName("Test User");
        existingProfile.setGamesPlayed(5);
        existingProfile.setGamesWon(3);
        existingProfile.setTotalScore(150);
    }

    @Test
    void createProfile_shouldCreateNewProfile() {
        CreateProfileRequest request = new CreateProfileRequest();
        request.setUserId(2L);
        request.setUsername("newuser");

        when(repository.existsById(2L)).thenReturn(false);
        when(repository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileDto result = service.createProfile(request);

        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getUsername()).isEqualTo("newuser");
        // displayName should default to username when not provided
        assertThat(result.getDisplayName()).isEqualTo("newuser");
    }

    @Test
    void createProfile_shouldUseProvidedDisplayName() {
        CreateProfileRequest request = new CreateProfileRequest();
        request.setUserId(2L);
        request.setUsername("newuser");
        request.setDisplayName("Custom Name");

        when(repository.existsById(2L)).thenReturn(false);
        when(repository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileDto result = service.createProfile(request);

        assertThat(result.getDisplayName()).isEqualTo("Custom Name");
    }

    @Test
    void createProfile_shouldThrowWhenProfileAlreadyExists() {
        CreateProfileRequest request = new CreateProfileRequest();
        request.setUserId(1L);
        request.setUsername("testuser");

        when(repository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.createProfile(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Profile already exists for this user");

        verify(repository, never()).save(any());
    }

    @Test
    void getProfile_shouldReturnProfileWhenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile));

        UserProfileDto result = service.getProfile(1L);

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getGamesPlayed()).isEqualTo(5);
    }

    @Test
    void getProfile_shouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Profile not found");
    }

    @Test
    void updateScore_shouldIncrementStatsWhenWon() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile));
        when(repository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileDto result = service.updateScore(1L, 50, true);

        assertThat(result.getGamesPlayed()).isEqualTo(6);  // 5 + 1
        assertThat(result.getGamesWon()).isEqualTo(4);     // 3 + 1
        assertThat(result.getTotalScore()).isEqualTo(200); // 150 + 50
    }

    @Test
    void updateScore_shouldNotIncrementWinsWhenLost() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingProfile));
        when(repository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileDto result = service.updateScore(1L, 20, false);

        assertThat(result.getGamesPlayed()).isEqualTo(6);  // incremented
        assertThat(result.getGamesWon()).isEqualTo(3);     // unchanged
        assertThat(result.getTotalScore()).isEqualTo(170); // 150 + 20
    }
}