package com.gameplatform.coinflipservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinGameRequest {

    @NotNull
    private Long playerId;

    @NotNull
    private String playerUsername;
}
