package com.gameplatform.userservice.controller;

import com.gameplatform.userservice.dto.CreateProfileRequest;
import com.gameplatform.userservice.dto.UserProfileDto;
import com.gameplatform.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    @PostMapping
    public ResponseEntity<UserProfileDto> createProfile(
            @Valid @RequestBody CreateProfileRequest request) {
        return ResponseEntity.ok(service.createProfile(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getProfile(userId));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserProfileDto> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(service.getProfileByUsername(username));
    }
}