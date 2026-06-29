package com.gameplatform.coinflipservice.service;

import com.gameplatform.coinflipservice.config.RabbitMQConfig;
import com.gameplatform.coinflipservice.dto.CreateGameRequest;
import com.gameplatform.coinflipservice.dto.GameDto;
import com.gameplatform.coinflipservice.dto.JoinGameRequest;
import com.gameplatform.coinflipservice.entity.Game;
import com.gameplatform.coinflipservice.event.GameFinishedEvent;
import com.gameplatform.coinflipservice.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    public GameDto createGame(CreateGameRequest request) {
        Game game = new Game();
        game.setCreatorId(request.getPlayerId());
        game.setCreatorUsername(request.getPlayerUsername());
        game.setCreatorPick(request.getPick());
        return toDto(gameRepository.save(game));
    }

    public GameDto joinGame(Long gameId, JoinGameRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (game.getStatus() != Game.GameStatus.WAITING) {
            throw new RuntimeException("Game is not available to join");
        }

        if (game.getCreatorId().equals(request.getPlayerId())) {
            throw new RuntimeException("You can't join your own game");
        }

        game.setOpponentId(request.getPlayerId());
        game.setOpponentUsername(request.getPlayerUsername());

        // flip the coin
        Game.CoinSide result = random.nextBoolean()
                ? Game.CoinSide.HEADS
                : Game.CoinSide.TAILS;
        game.setResult(result);
        game.setStatus(Game.GameStatus.FINISHED);

        // determine winner
        boolean creatorWon = result == game.getCreatorPick();
        Long winnerId   = creatorWon ? game.getCreatorId()      : request.getPlayerId();
        Long loserId    = creatorWon ? request.getPlayerId()     : game.getCreatorId();
        String winnerUsername = creatorWon ? game.getCreatorUsername() : request.getPlayerUsername();

        game.setWinnerId(winnerId);
        game.setWinnerUsername(winnerUsername);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                new GameFinishedEvent(winnerId, loserId, false, 50, 10)
        );

        return toDto(gameRepository.save(game));
    }

    public GameDto getGame(Long gameId) {
        return toDto(gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found")));
    }

    private GameDto toDto(Game game) {
        GameDto dto = new GameDto();
        dto.setId(game.getId());
        dto.setCreatorId(game.getCreatorId());
        dto.setCreatorUsername(game.getCreatorUsername());
        dto.setOpponentId(game.getOpponentId());
        dto.setOpponentUsername(game.getOpponentUsername());
        dto.setCreatorPick(game.getCreatorPick());
        dto.setResult(game.getResult());
        dto.setStatus(game.getStatus());
        dto.setWinnerId(game.getWinnerId());
        dto.setWinnerUsername(game.getWinnerUsername());
        dto.setCreatedAt(game.getCreatedAt());
        return dto;
    }
}
