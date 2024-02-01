package com.readnocry.rabbitmq.service.impl;

import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.WebTranslationProcessingDTO;
import com.readnocry.rabbitmq.service.WebRabbitConsumer;
import com.readnocry.web.service.WebDictionaryService;
import com.readnocry.web.service.WebTranslationService;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static com.readnocry.RabbitQueue.GET_DICTIONARY_RESPONSE;
import static com.readnocry.RabbitQueue.TRANSLATION_RESPONSE;

@Service
@Log4j
public class WebRabbitConsumerImpl implements WebRabbitConsumer {

    private final WebTranslationService translationService;
    private final WebDictionaryService webDictionaryService;

    public WebRabbitConsumerImpl(WebTranslationService translationService, WebDictionaryService webDictionaryService) {
        this.translationService = translationService;
        this.webDictionaryService = webDictionaryService;
    }

    @Override
    @RabbitListener(queues = TRANSLATION_RESPONSE)
    public void consumeTranslationResponse(WebTranslationProcessingDTO response) {
        try {
            log.info("Consuming translation response: " + response);
            translationService.processTranslationResponse(response);
        } catch (Exception e) {
            log.error("Error consuming translation response: " + response, e);
        }
    }

    @Override
    @RabbitListener(queues = GET_DICTIONARY_RESPONSE)
    public void consumeGetDictionaryResponse(DictionaryDTO dictionary) {
        try {
            log.info("Consuming get dictionary response: " + dictionary);
            webDictionaryService.processGetDictionaryResponse(dictionary);
        } catch (Exception e) {
            log.error("Error consuming get dictionary response: " + dictionary, e);
        }
    }
}
