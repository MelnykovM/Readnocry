package com.readnocry.rabbitmq.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.readnocry.RabbitQueue.CHAT_GPT_REQUEST;
import static com.readnocry.RabbitQueue.TRANSLATION_REQUEST;

@Configuration
public class RabbitConfiguration {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue chatGptRequestQueue() {
        return new Queue(CHAT_GPT_REQUEST);
    }

    @Bean
    public Queue chatGptTranslationRequestQueue() {
        return new Queue(TRANSLATION_REQUEST);
    }
}
