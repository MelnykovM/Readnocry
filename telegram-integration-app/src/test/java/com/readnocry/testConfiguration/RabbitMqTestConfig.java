package com.readnocry.testConfiguration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.testcontainers.containers.RabbitMQContainer;

import static com.readnocry.RabbitQueue.*;

@TestConfiguration
@ComponentScan(basePackages = "com.readnocry")
public class RabbitMqTestConfig {

    private static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:latest");

    static {
        rabbitMQContainer.start();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitMQContainer.getHost());
        connectionFactory.setPort(rabbitMQContainer.getAmqpPort());
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        var jsonRabbitTemplate = new RabbitTemplate(connectionFactory);
        jsonRabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return jsonRabbitTemplate;
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
    public Queue sendMailRequestQueue() {
        return new Queue(SEND_MAIL_REQUEST);
    }

    @Bean
    public Queue chatGptRequestQueue() {
        return new Queue(CHAT_GPT_REQUEST);
    }
}
