package com.gameplatform.tictactoeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinGameRequest {
    @NotNull
    private Long playerId;
    @NotBlank
    private String playerUsername;
}