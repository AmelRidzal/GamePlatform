package com.gameplatform.userservice.service;

import com.gameplatform.userservice.dto.CreateProfileRequest;
import com.gameplatform.userservice.dto.UserProfileDto;
import com.gameplatform.userservice.entity.UserProfile;
import com.gameplatform.userservice.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileDto createProfile(CreateProfileRequest request) {
        if (repository.existsById(request.getUserId())) {
            throw new RuntimeException("Profile already exists for this user");
        }

        UserProfile profile = new UserProfile();
        profile.setUserId(request.getUserId());
        profile.setUsername(request.getUsername());
        profile.setDisplayName(request.getDisplayName() != null
                ? request.getDisplayName()
                : request.getUsername());
        profile.setAvatarUrl(request.getAvatarUrl());

        return toDto(repository.save(profile));
    }

    public UserProfileDto getProfile(Long userId) {
        return repository.findById(userId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    public UserProfileDto getProfileByUsername(String username) {
        return repository.findByUsername(username)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    public UserProfileDto updateScore(Long userId, int scoreToAdd, boolean won) {
        UserProfile profile = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setGamesPlayed(profile.getGamesPlayed() + 1);
        profile.setTotalScore(profile.getTotalScore() + scoreToAdd);
        if (won) profile.setGamesWon(profile.getGamesWon() + 1);

        return toDto(repository.save(profile));
    }

    private UserProfileDto toDto(UserProfile profile) {
        UserProfileDto dto = new UserProfileDto();
        dto.setUserId(profile.getUserId());
        dto.setUsername(profile.getUsername());
        dto.setDisplayName(profile.getDisplayName());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setGamesPlayed(profile.getGamesPlayed());
        dto.setGamesWon(profile.getGamesWon());
        dto.setTotalScore(profile.getTotalScore());
        dto.setCreatedAt(profile.getCreatedAt());
        return dto;
    }
}