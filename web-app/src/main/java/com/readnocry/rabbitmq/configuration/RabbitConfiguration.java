package com.readnocry.rabbitmq.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.readnocry.RabbitQueue.GET_DICTIONARY_RESPONSE;
import static com.readnocry.RabbitQueue.TRANSLATION_RESPONSE;

@Configuration
public class RabbitConfiguration {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue webTranslationResponseQueue() {
        return new Queue(TRANSLATION_RESPONSE);
    }

    @Bean
    public Queue webGetDictionaryResponseQueue() {
        return new Queue(GET_DICTIONARY_RESPONSE);
    }
}
