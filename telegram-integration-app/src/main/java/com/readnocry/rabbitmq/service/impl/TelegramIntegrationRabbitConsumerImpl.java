package com.readnocry.rabbitmq.service.impl;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.WordDTO;
import com.readnocry.entity.TelegramMessage;
import com.readnocry.rabbitmq.service.TelegramIntegrationRabbitConsumer;
import com.readnocry.telegramIntegration.service.ChatGptService;
import com.readnocry.telegramIntegration.service.TelegramIntegrationService;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.readnocry.RabbitQueue.*;

@Service
@Log4j
public class TelegramIntegrationRabbitConsumerImpl implements TelegramIntegrationRabbitConsumer {

    private final TelegramIntegrationService telegramIntegrationService;
    private final ChatGptService chatGptService;

    public TelegramIntegrationRabbitConsumerImpl(TelegramIntegrationService telegramIntegrationService, ChatGptService chatGptService) {
        this.telegramIntegrationService = telegramIntegrationService;
        this.chatGptService = chatGptService;
    }

    @Override
    @RabbitListener(queues = TELEGRAM_TEXT_MESSAGE_REQUEST)
    public void consumeTelegramBotTextMessageUpdates(Update update) {
        try {
            log.info("Consuming telegram text message update for telegram user: " + update.getMessage().getFrom().getUserName());
            telegramIntegrationService.processTextMessage(update);
        } catch (Exception e) {
            log.error("Error consuming telegram text message update for telegram user: " + update.getMessage().getFrom().getUserName(), e);
        }
    }

    @Override
    @RabbitListener(queues = CHAT_GPT_RESPONSE)
    public void consumeChatGptResponse(ChatGptInteractionDTO response) {
        try {
            log.info("Consuming chatGPT response: " + response);
            chatGptService.processChatGptResponse(response);
        } catch (Exception e) {
            log.error("Error consuming chatGPT response: " + response, e);
        }
    }

    @Override
    @RabbitListener(queues = TRANSLATION_TO_TELEGRAM_REQUEST)
    public void consumeTranslationToTelegramRequest(WordDTO request) {
        try {
            log.info("Consuming translation to telegram request: " + request);
            telegramIntegrationService.processTranslationToTelegramRequest(request);
        } catch (Exception e) {
            log.error("Error consuming translation to telegram request: " + request, e);
        }
    }

    @Override
    @RabbitListener(queues = SAVE_TELEGRAM_MESSAGE_REQUEST)
    public void consumeTelegramBotSaveMessageRequest(TelegramMessage request) {
        try {
            log.info("Consuming telegram save message request: " + request);
            telegramIntegrationService.saveTelegramMessage(request);
        } catch (Exception e) {
            log.error("Error consuming telegram save message request: " + request, e);
        }
    }
}
