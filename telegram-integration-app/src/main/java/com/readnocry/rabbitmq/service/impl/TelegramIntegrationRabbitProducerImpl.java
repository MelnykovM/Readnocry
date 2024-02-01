package com.readnocry.rabbitmq.service.impl;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.MailParamsDTO;
import com.readnocry.rabbitmq.service.TelegramIntegrationRabbitProducer;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import static com.readnocry.RabbitQueue.*;

@Service
@Log4j
public class TelegramIntegrationRabbitProducerImpl implements TelegramIntegrationRabbitProducer {

    private final RabbitTemplate rabbitTemplate;

    public TelegramIntegrationRabbitProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produceTelegramBotResponse(SendMessage sendMessage) {
        try {
            log.info("Sending telegram response for telegram user chat ID: " + sendMessage.getChatId());
            rabbitTemplate.convertAndSend(TELEGRAM_RESPONSE, sendMessage);
        } catch (Exception e) {
            log.error("Error sending telegram response for telegram user chat ID: " + sendMessage.getChatId(), e);
        }
    }

    @Override
    public void produceTelegramDictionaryResponse(SendMessage sendMessage) {
        try {
            log.info("Sending telegram dictionary response for telegram user chat ID: " + sendMessage.getChatId());
            rabbitTemplate.convertAndSend(TELEGRAM_DICTIONARY_RESPONSE, sendMessage);
        } catch (Exception e) {
            log.error("Error sending telegram dictionary response for telegram user chat ID: " + sendMessage.getChatId(), e);
        }
    }

    @Override
    public void produceDeleteTelegramBotMessageRequest(DeleteMessage deleteMessage) {
        try {
            log.info("Sending telegram delete message request for telegram user chat ID: " + deleteMessage.getChatId());
            rabbitTemplate.convertAndSend(DELETE_TELEGRAM_MESSAGE_REQUEST, deleteMessage);
        } catch (Exception e) {
            log.error("Error sending telegram delete message request for telegram user chat ID: " + deleteMessage.getChatId(), e);
        }
    }

    @Override
    public void produceChatGptRequest(ChatGptInteractionDTO request) {
        try {
            log.info("Sending chatGPT request: " + request);
            rabbitTemplate.convertAndSend(CHAT_GPT_REQUEST, request);
        } catch (Exception e) {
            log.error("Error sending chatGPT request: " + request, e);
        }
    }

    @Override
    public void produceSendMailRequest(MailParamsDTO request) {
        try {
            log.info("Send mail request: " + request);
            rabbitTemplate.convertAndSend(SEND_MAIL_REQUEST, request);
        } catch (Exception e) {
            log.error("Error sending mail request: " + request, e);
        }
    }
}
