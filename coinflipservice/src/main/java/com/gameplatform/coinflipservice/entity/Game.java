package com.gameplatform.coinflipservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long creatorId;
    private String creatorUsername;

    private Long opponentId;
    private String opponentUsername;

    @Enumerated(EnumType.STRING)
    private CoinSide creatorPick;   // side the creator bet on

    @Enumerated(EnumType.STRING)
    private CoinSide result;        // actual flip result, null until played

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.WAITING;

    private Long winnerId;
    private String winnerUsername;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum CoinSide {
        HEADS, TAILS
    }

    public enum GameStatus {
        WAITING,
        FINISHED
    }
}
