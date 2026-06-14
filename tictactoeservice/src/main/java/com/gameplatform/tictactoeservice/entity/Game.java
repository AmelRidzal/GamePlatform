package com.gameplatform.tictactoeservice.entity;

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

    private Long playerXId;
    private String playerXUsername;

    private Long playerOId;
    private String playerOUsername;

    // board is stored as a 9-character string
    // '-' = empty, 'X' = X, 'O' = O
    // e.g. "X-O-X----"
    private String board = "---------";

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.WAITING;

    @Enumerated(EnumType.STRING)
    private GameSymbol currentTurn = GameSymbol.X;

    private String winner; // username of winner, or "DRAW"

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum GameStatus {
        WAITING,    // waiting for player 2
        IN_PROGRESS,
        FINISHED
    }

    public enum GameSymbol {
        X, O
    }
}