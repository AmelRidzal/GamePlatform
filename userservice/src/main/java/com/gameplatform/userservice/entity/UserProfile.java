package com.gameplatform.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;

    private String displayName;
    private String avatarUrl;
    private int gamesPlayed;
    private int gamesWon;
    private int totalScore;

    private LocalDateTime createdAt = LocalDateTime.now();
}