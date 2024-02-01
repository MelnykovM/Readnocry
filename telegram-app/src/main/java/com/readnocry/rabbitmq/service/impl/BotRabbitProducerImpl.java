package com.readnocry.rabbitmq.service.impl;

import com.readnocry.dto.TelegramMessageDTO;
import com.readnocry.rabbitmq.service.BotRabbitProducer;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.readnocry.RabbitQueue.SAVE_TELEGRAM_MESSAGE_REQUEST;
import static com.readnocry.RabbitQueue.TELEGRAM_TEXT_MESSAGE_REQUEST;

@Service
@Log4j
public class BotRabbitProducerImpl implements BotRabbitProducer {

    private final RabbitTemplate rabbitTemplate;

    public BotRabbitProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produceRequest(Update update) {
        try {
            log.info("Sending telegram message request for telegram user: " + update.getMessage().getFrom().getUserName());
            rabbitTemplate.convertAndSend(TELEGRAM_TEXT_MESSAGE_REQUEST, update);
        } catch (Exception e) {
            log.error("Error sending telegram message request for telegram user: " + update.getMessage().getFrom().getUserName(), e);
        }
    }

    @Override
    public void produceSaveMessageRequest(TelegramMessageDTO messageDTO) {
        try {
            log.info("Sending save telegram message request: " + messageDTO);
            rabbitTemplate.convertAndSend(SAVE_TELEGRAM_MESSAGE_REQUEST, messageDTO);
        } catch (Exception e) {
            log.error("Error sending save telegram message request: " + messageDTO, e);
        }
    }
}
