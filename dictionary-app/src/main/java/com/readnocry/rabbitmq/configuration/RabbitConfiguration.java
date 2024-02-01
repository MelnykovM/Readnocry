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
    public Queue translationToDictionaryRequestQueue() {
        return new Queue(TRANSLATION_TO_DICTIONARY_REQUEST);
    }

    @Bean
    public Queue getDictionaryRequestQueue() {
        return new Queue(GET_DICTIONARY_REQUEST);
    }

    @Bean
    public Queue deleteFromDictionaryResponseQueue() {
        return new Queue(DELETE_FROM_DICTIONARY_REQUEST);
    }
}
