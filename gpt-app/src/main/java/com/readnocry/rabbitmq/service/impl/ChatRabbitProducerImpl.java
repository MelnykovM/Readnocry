package com.readnocry.rabbitmq.service.impl;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.WebTranslationProcessingDTO;
import com.readnocry.rabbitmq.service.ChatRabbitProducer;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Log4j
@Service
public class ChatRabbitProducerImpl implements ChatRabbitProducer {

    private final RabbitTemplate rabbitTemplate;

    public ChatRabbitProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produceTelegramResponse(String rabbitQueue, ChatGptInteractionDTO interaction) {
        try {
            log.info("Sending Telegram response: " + interaction);
            rabbitTemplate.convertAndSend(rabbitQueue, interaction);
        } catch (Exception e) {
            log.error("Error sending Telegram response: " + interaction, e);
        }
    }

    @Override
    public void produceWebTranslationResponse(String rabbitQueue, WebTranslationProcessingDTO response) {
        try {
            log.info("Sending web translation response: " + response);
            rabbitTemplate.convertAndSend(rabbitQueue, response);
        } catch (Exception e) {
            log.error("Error sending web translation response: " + response, e);
        }
    }
}
