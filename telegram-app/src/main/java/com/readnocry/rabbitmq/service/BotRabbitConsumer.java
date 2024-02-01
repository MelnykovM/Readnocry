package com.readnocry.rabbitmq.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

public interface BotRabbitConsumer {

    void consumeResponse(SendMessage sendMessage);

    void consumeDeleteMessageRequest(DeleteMessage deleteMessage);

    void consumeDictionary(SendMessage sendMessage);
}
