package com.gameplatform.userservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileDto {
    private Long userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private int gamesPlayed;
    private int gamesWon;
    private int totalScore;
    private LocalDateTime createdAt;
}