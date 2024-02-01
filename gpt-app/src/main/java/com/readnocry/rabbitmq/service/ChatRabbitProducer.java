package com.readnocry.rabbitmq.service;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.WebTranslationProcessingDTO;

public interface ChatRabbitProducer {

    void produceTelegramResponse(String rabbitQueue, ChatGptInteractionDTO interaction);

    void produceWebTranslationResponse(String rabbitQueue, WebTranslationProcessingDTO response);
}
