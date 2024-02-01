package com.readnocry.rabbitmq.service.impl;

import com.readnocry.dto.MailParamsDTO;
import com.readnocry.mail.service.MailSenderService;
import com.readnocry.rabbitmq.service.MailRabbitConsumer;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static com.readnocry.RabbitQueue.SEND_MAIL_REQUEST;

@Log4j
@Service
public class MailRabbitConsumerImpl implements MailRabbitConsumer {

    private final MailSenderService mailSenderService;

    public MailRabbitConsumerImpl(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @Override
    @RabbitListener(queues = SEND_MAIL_REQUEST)
    public void consumeSendMailRequest(MailParamsDTO request) {
        try {
            log.info("Consuming send mail request: " + request);
            mailSenderService.processSendMailRequest(request);
        } catch (Exception e) {
            log.error("Error consuming send mail request: " + request, e);
        }
    }
}
