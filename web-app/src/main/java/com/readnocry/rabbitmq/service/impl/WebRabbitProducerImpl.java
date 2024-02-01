package com.readnocry.rabbitmq.service.impl;

import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.MailParamsDTO;
import com.readnocry.dto.WebTranslationProcessingDTO;
import com.readnocry.dto.WordDTO;
import com.readnocry.rabbitmq.service.WebRabbitProducer;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.readnocry.RabbitQueue.*;

@Service
@Log4j
public class WebRabbitProducerImpl implements WebRabbitProducer {

    private final RabbitTemplate rabbitTemplate;

    public WebRabbitProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produceTranslationRequest(WebTranslationProcessingDTO request) {
        try {
            log.info("Send translation request: " + request);
            rabbitTemplate.convertAndSend(TRANSLATION_REQUEST, request);
        } catch (Exception e) {
            log.error("Error sending translation request: " + request, e);
        }
    }

    @Override
    public void produceTranslationToDictionaryRequest(WordDTO wordDTO) {
        try {
            log.info("Send translation to dictionary request: " + wordDTO);
            rabbitTemplate.convertAndSend(TRANSLATION_TO_DICTIONARY_REQUEST, wordDTO);
        } catch (Exception e) {
            log.error("Error sending translation to dictionary request: " + wordDTO, e);
        }
    }

    @Override
    public void produceTranslationToTelegramRequest(WordDTO request) {
        try {
            log.info("Send translation to telegram request: " + request);
            rabbitTemplate.convertAndSend(TRANSLATION_TO_TELEGRAM_REQUEST, request);
        } catch (Exception e) {
            log.error("Error sending translation to telegram request: " + request, e);
        }
    }

    @Override
    public void produceGetDictionaryRequest(DictionaryDTO request) {
        try {
            log.info("Send get dictionary request: " + request);
            rabbitTemplate.convertAndSend(GET_DICTIONARY_REQUEST, request);
        } catch (Exception e) {
            log.error("Error sending get dictionary request: " + request, e);
        }
    }

    @Override
    public void produceDeleteFromDictionaryRequest(WordDTO request) {
        try {
            log.info("Send delete from dictionary request: " + request);
            rabbitTemplate.convertAndSend(DELETE_FROM_DICTIONARY_REQUEST, request);
        } catch (Exception e) {
            log.error("Error sending delete from dictionary request: " + request, e);
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
