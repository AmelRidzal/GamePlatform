package com.gameplatform.coinflipservice.dto;

import com.gameplatform.coinflipservice.entity.Game;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGameRequest {

    @NotNull
    private Long playerId;

    @NotNull
    private String playerUsername;

    @NotNull
    private Game.CoinSide pick;  // HEADS or TAILS
}
