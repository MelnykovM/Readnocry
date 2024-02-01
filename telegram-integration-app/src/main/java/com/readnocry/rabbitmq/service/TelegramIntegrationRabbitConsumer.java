package com.readnocry.rabbitmq.service;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.WordDTO;
import com.readnocry.entity.TelegramMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramIntegrationRabbitConsumer {

    void consumeTelegramBotTextMessageUpdates(Update update);

    void consumeChatGptResponse(ChatGptInteractionDTO response);

    void consumeTranslationToTelegramRequest(WordDTO request);

    void consumeTelegramBotSaveMessageRequest(TelegramMessage request);
}
