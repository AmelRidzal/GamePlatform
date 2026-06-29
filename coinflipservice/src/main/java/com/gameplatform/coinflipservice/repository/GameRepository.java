package com.gameplatform.coinflipservice.repository;

import com.gameplatform.coinflipservice.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
