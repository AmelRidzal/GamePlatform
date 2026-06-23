package com.gameplatform.tictactoeservice.service;

import com.gameplatform.tictactoeservice.config.RabbitMQConfig;
import com.gameplatform.tictactoeservice.dto.*;
import com.gameplatform.tictactoeservice.entity.Game;
import com.gameplatform.tictactoeservice.event.GameFinishedEvent;
import com.gameplatform.tictactoeservice.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;

    public GameDto createGame(CreateGameRequest request) {
        Game game = new Game();
        game.setPlayerXId(request.getPlayerId());
        game.setPlayerXUsername(request.getPlayerUsername());
        return toDto(gameRepository.save(game));
    }

    public GameDto joinGame(Long gameId, JoinGameRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (game.getStatus() != Game.GameStatus.WAITING) {
            throw new RuntimeException("Game is not available to join");
        }

        if (game.getPlayerXId().equals(request.getPlayerId())) {
            throw new RuntimeException("You can't join your own game");
        }

        game.setPlayerOId(request.getPlayerId());
        game.setPlayerOUsername(request.getPlayerUsername());
        game.setStatus(Game.GameStatus.IN_PROGRESS);

        GameDto dto = toDto(gameRepository.save(game));

        // notify both players via WebSocket that game has started
        messagingTemplate.convertAndSend("/topic/game/" + gameId, dto);

        return dto;
    }

    public GameDto makeMove(Long gameId, MakeMoveRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        // validate game state
        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            throw new RuntimeException("Game is not in progress");
        }

        // validate it's the right player's turn
        boolean isPlayerX = game.getPlayerXId().equals(request.getPlayerId());
        boolean isPlayerO = game.getPlayerOId().equals(request.getPlayerId());

        if (!isPlayerX && !isPlayerO) {
            throw new RuntimeException("You are not a player in this game");
        }

        Game.GameSymbol playerSymbol = isPlayerX ? Game.GameSymbol.X : Game.GameSymbol.O;

        if (game.getCurrentTurn() != playerSymbol) {
            throw new RuntimeException("It's not your turn");
        }

        // validate position
        char[] board = game.getBoard().toCharArray();
        int position = request.getPosition();

        if (board[position] != '-') {
            throw new RuntimeException("Position already taken");
        }

        // make the move
        board[position] = playerSymbol == Game.GameSymbol.X ? 'X' : 'O';
        game.setBoard(new String(board));
        game.setUpdatedAt(LocalDateTime.now());

        // check for win or draw
        String winner = checkWinner(board);

        if (winner != null) {
            game.setStatus(Game.GameStatus.FINISHED);

            if (winner.equals("DRAW")) {
                game.setWinner("DRAW");

                // publish draw event for both players
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY,
                        new GameFinishedEvent(
                                game.getPlayerXId(),
                                game.getPlayerOId(),
                                true, 10, 10  // draw — both get 10 points
                        )
                );

            } else {
                // figure out who won and who lost
                boolean xWon = winner.equals("X");
                Long winnerId = xWon ? game.getPlayerXId() : game.getPlayerOId();
                Long loserId  = xWon ? game.getPlayerOId() : game.getPlayerXId();
                String winnerUsername = xWon ? game.getPlayerXUsername() : game.getPlayerOUsername();

                game.setWinner(winnerUsername);

                // publish win event
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY,
                        new GameFinishedEvent(
                                winnerId,
                                loserId,
                                false, 50, 10  // winner gets 50, loser gets 10
                        )
                );
            }
        } else {
            // switch turns
            game.setCurrentTurn(
                    game.getCurrentTurn() == Game.GameSymbol.X
                            ? Game.GameSymbol.O
                            : Game.GameSymbol.X
            );
        }

        GameDto dto = toDto(gameRepository.save(game));

        // broadcast updated game state to both players
        messagingTemplate.convertAndSend("/topic/game/" + gameId, dto);

        return dto;
    }

    public GameDto getGame(Long gameId) {
        return toDto(gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found")));
    }

    private String checkWinner(char[] b) {
        // all winning combinations
        int[][] wins = {
                {0,1,2}, {3,4,5}, {6,7,8}, // rows
                {0,3,6}, {1,4,7}, {2,5,8}, // columns
                {0,4,8}, {2,4,6}            // diagonals
        };

        for (int[] win : wins) {
            if (b[win[0]] != '-' &&
                    b[win[0]] == b[win[1]] &&
                    b[win[1]] == b[win[2]]) {
                return String.valueOf(b[win[0]]); // "X" or "O"
            }
        }

        // check for draw — no empty spaces left
        for (char c : b) {
            if (c == '-') return null; // game still going
        }

        return "DRAW";
    }

    private GameDto toDto(Game game) {
        GameDto dto = new GameDto();
        dto.setId(game.getId());
        dto.setPlayerXId(game.getPlayerXId());
        dto.setPlayerXUsername(game.getPlayerXUsername());
        dto.setPlayerOId(game.getPlayerOId());
        dto.setPlayerOUsername(game.getPlayerOUsername());
        dto.setBoard(game.getBoard());
        dto.setStatus(game.getStatus());
        dto.setCurrentTurn(game.getCurrentTurn());
        dto.setWinner(game.getWinner());
        dto.setCreatedAt(game.getCreatedAt());
        return dto;
    }
}