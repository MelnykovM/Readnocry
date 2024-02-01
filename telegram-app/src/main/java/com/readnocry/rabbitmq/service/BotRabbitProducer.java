package com.readnocry.rabbitmq.service;

import com.readnocry.dto.TelegramMessageDTO;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotRabbitProducer {

    void produceRequest(Update update);

    void produceSaveMessageRequest(TelegramMessageDTO messageDTO);
}