package com.readnocry.rabbitmq.service.impl;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.WebTranslationProcessingDTO;
import com.readnocry.gpt.service.ChatGptService;
import com.readnocry.rabbitmq.service.ChatRabbitConsumer;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static com.readnocry.RabbitQueue.CHAT_GPT_REQUEST;
import static com.readnocry.RabbitQueue.TRANSLATION_REQUEST;

@Log4j
@Service
public class ChatRabbitConsumerImpl implements ChatRabbitConsumer {

    private final ChatGptService chatGptService;

    public ChatRabbitConsumerImpl(ChatGptService chatGptService) {
        this.chatGptService = chatGptService;
    }

    @Override
    @RabbitListener(queues = CHAT_GPT_REQUEST)
    public void consumeInteraction(ChatGptInteractionDTO request) {
        try {
            log.info("Consuming ChatGPT interaction request: " + request);
            chatGptService.processInteraction(request);
        } catch (Exception e) {
            log.error("Error processing ChatGPT interaction: " + request, e);
        }
    }

    @RabbitListener(queues = TRANSLATION_REQUEST)
    @Override
    public void consumeTranslationRequest(WebTranslationProcessingDTO request) {
        try {
            log.info("Consuming translation request: " + request);
            chatGptService.processWebTranslationRequest(request);
        } catch (Exception e) {
            log.error("Error processing translation request: " + request, e);
        }
    }
}
