package com.readnocry.telegram.service.impl;

import com.readnocry.dto.TelegramMessageDTO;
import com.readnocry.rabbitmq.service.BotRabbitProducer;
import com.readnocry.telegram.controller.TelegramBot;
import com.readnocry.telegram.service.TelegramBotService;
import com.readnocry.telegram.utils.MessageUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Log4j
public class TelegramBotServiceImpl implements TelegramBotService {

    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final BotRabbitProducer botRabbitProducer;

    public TelegramBotServiceImpl(MessageUtils messageUtils, BotRabbitProducer botRabbitProducer) {
        this.messageUtils = messageUtils;
        this.botRabbitProducer = botRabbitProducer;
    }

    @Override
    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void processUpdate(Update update) {
        log.info("Start process update: " + update);
        if (update == null) {
            log.error("Received update is null");
            return;
        }
        distributeMessagesByType(update);
    }

    private void distributeMessagesByType(Update update) {
        var message = update.getMessage();
        if (message.hasText()) {
            processTextMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setUnsupportedMessageTypeView(Update update) {
        log.error("Unsupported message type is received: " + update);
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Unsupported message type!");
        sendMessage(sendMessage);
    }

    @Override
    public void sendMessage(SendMessage sendMessage) {
        log.info("Send message: " + sendMessage);
        telegramBot.sendAnswerMessage(sendMessage);
    }

    @Override
    public void sendDictionaryMessage(SendMessage sendMessage) {
        log.info("Send dictionary message: " + sendMessage);
        telegramBot.sendDictionaryMessage(sendMessage);
    }

    @Override
    public void saveMessage(Message message) {
        log.info("Save message: " + message);
        Integer messageId = message.getMessageId();
        Long chatId = message.getChatId();
        botRabbitProducer.produceSaveMessageRequest(new TelegramMessageDTO(chatId, messageId));
    }

    @Override
    public void deleteMessage(DeleteMessage deleteMessage) {
        log.info("Delete message: " + deleteMessage);
        telegramBot.deleteMessage(deleteMessage);
    }

    private void processTextMessage(Update update) {
        log.info("Process text message: " + update);
        botRabbitProducer.produceRequest(update);
    }
}
