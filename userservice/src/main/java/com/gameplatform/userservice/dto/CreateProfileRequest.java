package com.gameplatform.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProfileRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String username;

    private String displayName;
    private String avatarUrl;
}