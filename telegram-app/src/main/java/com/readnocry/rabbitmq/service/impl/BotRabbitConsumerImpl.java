package com.readnocry.rabbitmq.service.impl;

import com.readnocry.rabbitmq.service.BotRabbitConsumer;
import com.readnocry.telegram.service.TelegramBotService;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import static com.readnocry.RabbitQueue.*;

@Service
@Log4j
public class BotRabbitConsumerImpl implements BotRabbitConsumer {

    private final TelegramBotService telegramBotService;

    public BotRabbitConsumerImpl(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @Override
    @RabbitListener(queues = TELEGRAM_RESPONSE)
    public void consumeResponse(SendMessage sendMessage) {
        try {
            log.info("Consuming telegram response for telegram user chat ID: " + sendMessage.getChatId());
            telegramBotService.sendMessage(sendMessage);
        } catch (Exception e) {
            log.error("Error consuming telegram response for telegram user chat ID: " + sendMessage.getChatId(), e);
        }
    }

    @Override
    @RabbitListener(queues = TELEGRAM_DICTIONARY_RESPONSE)
    public void consumeDictionary(SendMessage sendMessage) {
        try {
            log.info("Consuming telegram dictionary response for telegram user chat ID: " + sendMessage.getChatId());
            telegramBotService.sendDictionaryMessage(sendMessage);
        } catch (Exception e) {
            log.error("Error consuming telegram dictionary response for telegram user chat ID: " + sendMessage.getChatId(), e);
        }
    }

    @Override
    @RabbitListener(queues = DELETE_TELEGRAM_MESSAGE_REQUEST)
    public void consumeDeleteMessageRequest(DeleteMessage deleteMessage) {
        try {
            log.info("Consuming delete telegram message request for telegram user chat ID: " + deleteMessage.getChatId());
            telegramBotService.deleteMessage(deleteMessage);
        } catch (Exception e) {
            log.error("Error consuming delete telegram message request for telegram user chat ID: " + deleteMessage.getChatId(), e);
        }
    }
}
