package com.gameplatform.tictactoeservice.repository;

import com.gameplatform.tictactoeservice.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByPlayerXIdOrPlayerOId(Long playerXId, Long playerOId);
    List<Game> findByStatus(Game.GameStatus status);
}