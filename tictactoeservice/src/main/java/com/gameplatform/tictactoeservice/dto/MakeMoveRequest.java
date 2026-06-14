package com.gameplatform.tictactoeservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MakeMoveRequest {
    @NotNull
    private Long playerId;

    @NotNull
    @Min(0) @Max(8)
    private Integer position; // 0-8 on the board
}