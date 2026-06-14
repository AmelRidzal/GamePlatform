package com.gameplatform.tictactoeservice.dto;

import com.gameplatform.tictactoeservice.entity.Game;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GameDto {
    private Long id;
    private Long playerXId;
    private String playerXUsername;
    private Long playerOId;
    private String playerOUsername;
    private String board;
    private Game.GameStatus status;
    private Game.GameSymbol currentTurn;
    private String winner;
    private LocalDateTime createdAt;

    // visual representation of the board for easy reading
    public String getBoardDisplay() {
        char[] b = board.toCharArray();
        return String.format(
                " %s | %s | %s \n-----------\n %s | %s | %s \n-----------\n %s | %s | %s ",
                b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8]
        );
    }
}