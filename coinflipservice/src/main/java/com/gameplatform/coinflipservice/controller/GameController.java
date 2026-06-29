package com.gameplatform.coinflipservice.controller;

import com.gameplatform.coinflipservice.dto.CreateGameRequest;
import com.gameplatform.coinflipservice.dto.GameDto;
import com.gameplatform.coinflipservice.dto.JoinGameRequest;
import com.gameplatform.coinflipservice.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coinflip")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

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
}
