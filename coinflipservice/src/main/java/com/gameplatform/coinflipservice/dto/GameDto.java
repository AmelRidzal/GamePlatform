package com.gameplatform.coinflipservice.dto;

import com.gameplatform.coinflipservice.entity.Game;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GameDto {
    private Long id;
    private Long creatorId;
    private String creatorUsername;
    private Long opponentId;
    private String opponentUsername;
    private Game.CoinSide creatorPick;
    private Game.CoinSide result;
    private Game.GameStatus status;
    private Long winnerId;
    private String winnerUsername;
    private LocalDateTime createdAt;
}
