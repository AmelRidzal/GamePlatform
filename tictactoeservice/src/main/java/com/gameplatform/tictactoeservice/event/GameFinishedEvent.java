package com.gameplatform.tictactoeservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameFinishedEvent implements Serializable {
    private Long winnerId;
    private Long loserId;
    private boolean isDraw;
    private int scoreForWinner;
    private int scoreForLoser;
}