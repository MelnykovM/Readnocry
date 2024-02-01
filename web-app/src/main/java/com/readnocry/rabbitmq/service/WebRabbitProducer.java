package com.readnocry.rabbitmq.service;

import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.MailParamsDTO;
import com.readnocry.dto.WebTranslationProcessingDTO;
import com.readnocry.dto.WordDTO;

public interface WebRabbitProducer {

    void produceTranslationRequest(WebTranslationProcessingDTO request);

    void produceTranslationToDictionaryRequest(WordDTO wordDTO);

    void produceTranslationToTelegramRequest(WordDTO request);

    void produceGetDictionaryRequest(DictionaryDTO request);

    void produceDeleteFromDictionaryRequest(WordDTO request);

    void produceSendMailRequest(MailParamsDTO request);
}
