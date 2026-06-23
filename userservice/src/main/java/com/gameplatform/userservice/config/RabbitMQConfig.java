package com.gameplatform.userservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "game-finished-queue";
    public static final String EXCHANGE_NAME = "game-events-exchange";
    public static final String ROUTING_KEY = "game.finished";

    @Bean
    public DirectExchange gameEventsExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue gameFinishedQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding binding(Queue gameFinishedQueue, DirectExchange gameEventsExchange) {
        return BindingBuilder.bind(gameFinishedQueue)
                .to(gameEventsExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}