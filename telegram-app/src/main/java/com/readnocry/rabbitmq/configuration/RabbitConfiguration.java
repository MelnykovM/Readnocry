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
    public Queue telegramResponseQueue() {
        return new Queue(TELEGRAM_RESPONSE);
    }

    @Bean
    public Queue telegramDictionaryResponseQueue() {
        return new Queue(TELEGRAM_DICTIONARY_RESPONSE);
    }

    @Bean
    public Queue deleteMessageQueue() {
        return new Queue(DELETE_TELEGRAM_MESSAGE_REQUEST);
    }
}
