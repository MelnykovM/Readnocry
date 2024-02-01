package com.readnocry.rabbitmq.service;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.WebTranslationProcessingDTO;

public interface ChatRabbitConsumer {

    void consumeInteraction(ChatGptInteractionDTO request);

    void consumeTranslationRequest(WebTranslationProcessingDTO request);
}
