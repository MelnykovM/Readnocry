package com.readnocry.rabbitmq.service.impl;

import com.readnocry.dto.DictionaryDTO;
import com.readnocry.rabbitmq.service.DictionaryRabbitProducer;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.readnocry.RabbitQueue.GET_DICTIONARY_RESPONSE;

@Service
@Log4j
public class DictionaryRabbitProducerImpl implements DictionaryRabbitProducer {

    private final RabbitTemplate rabbitTemplate;

    public DictionaryRabbitProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produceGetDictionaryResponse(DictionaryDTO dictionary) {
        try {
            log.info("Sending get dictionary response: " + dictionary);
            rabbitTemplate.convertAndSend(GET_DICTIONARY_RESPONSE, dictionary);
        } catch (Exception e) {
            log.error("Error sending get dictionary response: " + dictionary, e);
        }
    }
}
