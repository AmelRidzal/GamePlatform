package com.gameplatform.tictactoeservice.service;

import com.gameplatform.tictactoeservice.dto.*;
import com.gameplatform.tictactoeservice.entity.Game;
import com.gameplatform.tictactoeservice.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GameService gameService;

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.setId(1L);
        game.setPlayerXId(1L);
        game.setPlayerXUsername("playerX");
        game.setPlayerOId(2L);
        game.setPlayerOUsername("playerO");
        game.setBoard("---------");
        game.setStatus(Game.GameStatus.IN_PROGRESS);
        game.setCurrentTurn(Game.GameSymbol.X);
    }

    @Test
    void createGame_shouldSaveNewGameWithPlayerX() {
        CreateGameRequest request = new CreateGameRequest();
        request.setPlayerId(1L);
        request.setPlayerUsername("playerX");

        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game g = invocation.getArgument(0);
            g.setId(1L);
            return g;
        });

        GameDto result = gameService.createGame(request);

        assertThat(result.getPlayerXId()).isEqualTo(1L);
        assertThat(result.getPlayerXUsername()).isEqualTo("playerX");
        assertThat(result.getStatus()).isEqualTo(Game.GameStatus.WAITING);
        assertThat(result.getBoard()).isEqualTo("---------");
    }

    @Test
    void makeMove_shouldPlaceSymbolAndSwitchTurn() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        MakeMoveRequest request = new MakeMoveRequest();
        request.setPlayerId(1L); // playerX
        request.setPosition(4);

        GameDto result = gameService.makeMove(1L, request);

        assertThat(result.getBoard().charAt(4)).isEqualTo('X');
        assertThat(result.getCurrentTurn()).isEqualTo(Game.GameSymbol.O); // turn switched
        assertThat(result.getStatus()).isEqualTo(Game.GameStatus.IN_PROGRESS);
    }

    @Test
    void makeMove_shouldThrowWhenNotPlayersTurn() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        MakeMoveRequest request = new MakeMoveRequest();
        request.setPlayerId(2L); // playerO trying to move on X's turn
        request.setPosition(4);

        assertThatThrownBy(() -> gameService.makeMove(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("It's not your turn");
    }

    @Test
    void makeMove_shouldThrowWhenPositionTaken() {
        game.setBoard("X--------"); // position 0 already taken

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        MakeMoveRequest request = new MakeMoveRequest();
        request.setPlayerId(2L);
        request.setPosition(0);
        game.setCurrentTurn(Game.GameSymbol.O); // make it O's turn so we get past that check

        assertThatThrownBy(() -> gameService.makeMove(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Position already taken");
    }

    @Test
    void makeMove_shouldDetectHorizontalWin() {
        game.setBoard("XX-------"); // X has 0,1 — about to win with position 2

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        MakeMoveRequest request = new MakeMoveRequest();
        request.setPlayerId(1L);
        request.setPosition(2);

        GameDto result = gameService.makeMove(1L, request);

        assertThat(result.getStatus()).isEqualTo(Game.GameStatus.FINISHED);
        assertThat(result.getWinner()).isEqualTo("playerX");
    }

    @Test
    void makeMove_shouldDetectDraw() {
        // X O X / X O O / O X -  → one move left at position 8 by X, results in draw
        game.setBoard("XOXXOOOX-");

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        MakeMoveRequest request = new MakeMoveRequest();
        request.setPlayerId(1L);
        request.setPosition(8);

        GameDto result = gameService.makeMove(1L, request);

        assertThat(result.getStatus()).isEqualTo(Game.GameStatus.FINISHED);
        assertThat(result.getWinner()).isEqualTo("DRAW");
    }

    @Test
    void joinGame_shouldThrowWhenPlayerTriesToJoinOwnGame() {
        Game waitingGame = new Game();
        waitingGame.setId(1L);
        waitingGame.setPlayerXId(1L);
        waitingGame.setPlayerXUsername("playerX");
        waitingGame.setStatus(Game.GameStatus.WAITING);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));

        JoinGameRequest request = new JoinGameRequest();
        request.setPlayerId(1L); // same as playerX
        request.setPlayerUsername("playerX");

        assertThatThrownBy(() -> gameService.joinGame(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You can't join your own game");
    }
}