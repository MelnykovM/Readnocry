package com.readnocry.rabbitmq.service.impl;

import com.readnocry.dictionary.service.DictionaryService;
import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.WordDTO;
import com.readnocry.rabbitmq.service.DictionaryRabbitConsumer;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static com.readnocry.RabbitQueue.*;

@Service
@Log4j
public class DictionaryRabbitConsumerImpl implements DictionaryRabbitConsumer {

    private final DictionaryService dictionaryService;

    public DictionaryRabbitConsumerImpl(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    @RabbitListener(queues = TRANSLATION_TO_DICTIONARY_REQUEST)
    public void consumeTranslationToDictionaryRequest(WordDTO request) {
        try {
            log.info("Consuming translation to dictionary request: " + request);
            dictionaryService.addWordToUserDictionary(request);
        } catch (Exception e) {
            log.error("Error consuming translation to dictionary request: " + request, e);
        }
    }

    @Override
    @RabbitListener(queues = GET_DICTIONARY_REQUEST)
    public void consumeGetDictionaryRequest(DictionaryDTO request) {
        try {
            log.info("Consuming get dictionary request: " + request);
            dictionaryService.consumeGetDictionaryRequest(request);
        } catch (Exception e) {
            log.error("Error consuming get dictionary request: " + request, e);
        }
    }

    @Override
    @RabbitListener(queues = DELETE_FROM_DICTIONARY_REQUEST)
    public void consumeDeleteFromDictionaryRequest(WordDTO request) {
        try {
            log.info("Consuming delete from dictionary request: " + request);
            dictionaryService.deleteFromDictionary(request);
        } catch (Exception e) {
            log.error("Error consuming delete from dictionary request: " + request, e);
        }
    }
}
