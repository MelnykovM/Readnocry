package com.readnocry.rabbitmq.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.readnocry.RabbitQueue.*;

@Configuration
public class RabbitConfiguration {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue textMessageQueue() {
        return new Queue(TELEGRAM_TEXT_MESSAGE_REQUEST);
    }

    @Bean
    public Queue chatAnswerMessageQueue() {
        return new Queue(CHAT_GPT_RESPONSE);
    }

    @Bean
    public Queue webTranslationToDictionaryQueue() {
        return new Queue(TRANSLATION_TO_TELEGRAM_REQUEST);
    }

    @Bean
    public Queue saveTelegramMessageQueue() {
        return new Queue(SAVE_TELEGRAM_MESSAGE_REQUEST);
    }
}
