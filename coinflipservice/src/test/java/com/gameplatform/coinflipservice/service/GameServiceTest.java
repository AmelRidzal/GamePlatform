package com.gameplatform.coinflipservice.service;

import com.gameplatform.coinflipservice.config.RabbitMQConfig;
import com.gameplatform.coinflipservice.dto.CreateGameRequest;
import com.gameplatform.coinflipservice.dto.GameDto;
import com.gameplatform.coinflipservice.dto.JoinGameRequest;
import com.gameplatform.coinflipservice.entity.Game;
import com.gameplatform.coinflipservice.event.GameFinishedEvent;
import com.gameplatform.coinflipservice.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private GameService gameService;

    private Game waitingGame;

    @BeforeEach
    void setUp() {
        waitingGame = new Game();
        waitingGame.setId(1L);
        waitingGame.setCreatorId(1L);
        waitingGame.setCreatorUsername("alice");
        waitingGame.setCreatorPick(Game.CoinSide.HEADS);
        waitingGame.setStatus(Game.GameStatus.WAITING);
    }

    // ─── createGame ───────────────────────────────────────────────────────────

    @Test
    void createGame_shouldSaveGameWithCorrectFields() {
        CreateGameRequest request = new CreateGameRequest();
        request.setPlayerId(1L);
        request.setPlayerUsername("alice");
        request.setPick(Game.CoinSide.HEADS);

        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game g = invocation.getArgument(0);
            g.setId(1L);
            return g;
        });

        GameDto result = gameService.createGame(request);

        assertThat(result.getCreatorId()).isEqualTo(1L);
        assertThat(result.getCreatorUsername()).isEqualTo("alice");
        assertThat(result.getCreatorPick()).isEqualTo(Game.CoinSide.HEADS);
        assertThat(result.getStatus()).isEqualTo(Game.GameStatus.WAITING);
        assertThat(result.getResult()).isNull();
        assertThat(result.getWinnerId()).isNull();
    }

    @Test
    void createGame_shouldNotPublishRabbitEvent() {
        CreateGameRequest request = new CreateGameRequest();
        request.setPlayerId(1L);
        request.setPlayerUsername("alice");
        request.setPick(Game.CoinSide.TAILS);

        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        gameService.createGame(request);

        // no rabbit event should fire on game creation — only on join
        verifyNoInteractions(rabbitTemplate);
    }

    // ─── joinGame ─────────────────────────────────────────────────────────────

    @Test
    void joinGame_shouldFinishGameAndSetResult() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        JoinGameRequest request = new JoinGameRequest();
        request.setPlayerId(2L);
        request.setPlayerUsername("bob");

        GameDto result = gameService.joinGame(1L, request);

        assertThat(result.getStatus()).isEqualTo(Game.GameStatus.FINISHED);
        assertThat(result.getResult()).isNotNull(); // HEADS or TAILS
        assertThat(result.getOpponentId()).isEqualTo(2L);
        assertThat(result.getOpponentUsername()).isEqualTo("bob");
    }

    @Test
    void joinGame_shouldSetWinnerCorrectly() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        JoinGameRequest request = new JoinGameRequest();
        request.setPlayerId(2L);
        request.setPlayerUsername("bob");

        GameDto result = gameService.joinGame(1L, request);

        // winner must be one of the two players
        assertThat(result.getWinnerId()).isIn(1L, 2L);
        assertThat(result.getWinnerUsername()).isIn("alice", "bob");
    }

    @Test
    void joinGame_creatorWins_whenResultMatchesPick() {
        // force the result to match creator's pick (HEADS) by using a seeded approach:
        // we verify the logic directly — if result == creatorPick, creator wins
        waitingGame.setCreatorPick(Game.CoinSide.HEADS);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        JoinGameRequest request = new JoinGameRequest();
        request.setPlayerId(2L);
        request.setPlayerUsername("bob");

        GameDto result = gameService.joinGame(1L, request);

        // if result is HEADS, alice (creator) wins; if TAILS, bob wins
        if (result.getResult() == Game.CoinSide.HEADS) {
            assertThat(result.getWinnerId()).isEqualTo(1L);
            assertThat(result.getWinnerUsername()).isEqualTo("alice");
        } else {
            assertThat(result.getWinnerId()).isEqualTo(2L);
            assertThat(result.getWinnerUsername()).isEqualTo("bob");
        }
    }

    @Test
    void joinGame_shouldPublishRabbitEventWithCorrectScores() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        JoinGameRequest request = new JoinGameRequest();
        request.setPlayerId(2L);
        request.setPlayerUsername("bob");

        gameService.joinGame(1L, request);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY),
                (Object) argThat((GameFinishedEvent e) ->
                        e.getScoreForWinner() == 50
                                && e.getScoreForLoser() == 10
                                && !e.isDraw()
                )
        );
    }

    @Test
    void joinGame_shouldThrowWhenGameAlreadyFinished() {
        waitingGame.setStatus(Game.GameStatus.FINISHED);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));

        JoinGameRequest request = new JoinGameRequest();
        request.setPlayerId(2L);
        request.setPlayerUsername("bob");

        assertThatThrownBy(() -> gameService.joinGame(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Game is not available to join");
    }

    @Test
    void joinGame_shouldThrowWhenPlayerJoinsOwnGame() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));

        JoinGameRequest request = new JoinGameRequest();
        request.setPlayerId(1L); // same as creatorId
        request.setPlayerUsername("alice");

        assertThatThrownBy(() -> gameService.joinGame(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You can't join your own game");
    }

    // ─── getGame ──────────────────────────────────────────────────────────────

    @Test
    void getGame_shouldReturnCorrectGame() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(waitingGame));

        GameDto result = gameService.getGame(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreatorUsername()).isEqualTo("alice");
    }

    @Test
    void getGame_shouldThrowWhenGameNotFound() {
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getGame(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Game not found");
    }
}