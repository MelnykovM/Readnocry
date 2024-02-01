package com.readnocry.rabbitmq.service;

import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.WebTranslationProcessingDTO;

public interface WebRabbitConsumer {

    void consumeTranslationResponse(WebTranslationProcessingDTO response);

    void consumeGetDictionaryResponse(DictionaryDTO dictionary);
}
