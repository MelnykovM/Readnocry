package com.readnocry.rabbitmq.service;

import com.readnocry.dto.DictionaryDTO;

public interface DictionaryRabbitProducer {

    void produceGetDictionaryResponse(DictionaryDTO dictionary);
}
