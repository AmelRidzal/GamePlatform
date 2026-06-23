package com.gameplatform.userservice.event;

import com.gameplatform.userservice.config.RabbitMQConfig;
import com.gameplatform.userservice.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEventListener {

    private final UserProfileService userProfileService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleGameFinished(GameFinishedEvent event) {
        log.info("Received game finished event: {}", event);

        try {
            if (event.isDraw()) {
                // both players get draw score
                userProfileService.updateScore(event.getWinnerId(), event.getScoreForWinner(), false);
                userProfileService.updateScore(event.getLoserId(), event.getScoreForLoser(), false);
            } else {
                // winner
                userProfileService.updateScore(event.getWinnerId(), event.getScoreForWinner(), true);
                // loser
                userProfileService.updateScore(event.getLoserId(), event.getScoreForLoser(), false);
            }
        } catch (Exception e) {
            log.error("Failed to update scores for game event: {}", event, e);
        }
    }
}