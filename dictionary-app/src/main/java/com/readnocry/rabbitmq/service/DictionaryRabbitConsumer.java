package com.readnocry.rabbitmq.service;

import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.WordDTO;

public interface DictionaryRabbitConsumer {

    void consumeTranslationToDictionaryRequest(WordDTO request);

    void consumeGetDictionaryRequest(DictionaryDTO request);

    void consumeDeleteFromDictionaryRequest(WordDTO request);
}
