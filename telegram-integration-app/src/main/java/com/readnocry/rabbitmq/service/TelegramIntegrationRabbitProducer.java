package com.readnocry.rabbitmq.service;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.MailParamsDTO;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

public interface TelegramIntegrationRabbitProducer {

    void produceChatGptRequest(ChatGptInteractionDTO request);

    void produceTelegramBotResponse(SendMessage sendMessage);

    void produceDeleteTelegramBotMessageRequest(DeleteMessage deleteMessage);

    void produceTelegramDictionaryResponse(SendMessage sendMessage);

    void produceSendMailRequest(MailParamsDTO request);
}