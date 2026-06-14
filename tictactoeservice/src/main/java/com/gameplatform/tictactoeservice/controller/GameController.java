package com.gameplatform.tictactoeservice.controller;

import com.gameplatform.tictactoeservice.dto.*;
import com.gameplatform.tictactoeservice.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tictactoe")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    // REST endpoints
    @PostMapping("/create")
    public ResponseEntity<GameDto> createGame(@Valid @RequestBody CreateGameRequest request) {
        return ResponseEntity.ok(gameService.createGame(request));
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameDto> joinGame(@PathVariable Long gameId,
                                            @Valid @RequestBody JoinGameRequest request) {
        return ResponseEntity.ok(gameService.joinGame(gameId, request));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDto> getGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getGame(gameId));
    }

    // WebSocket endpoint — clients send moves to /app/game/{gameId}/move
    @MessageMapping("/game/{gameId}/move")
    public void makeMove(@DestinationVariable Long gameId,
                         @Valid MakeMoveRequest request) {
        gameService.makeMove(gameId, request);
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameDto> makeMoveRest(@PathVariable Long gameId,
                                                @Valid @RequestBody MakeMoveRequest request) {
        return ResponseEntity.ok(gameService.makeMove(gameId, request));
    }
}