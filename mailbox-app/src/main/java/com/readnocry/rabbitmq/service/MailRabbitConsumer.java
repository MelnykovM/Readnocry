package com.readnocry.rabbitmq.service;

import com.readnocry.dto.MailParamsDTO;

public interface MailRabbitConsumer {
    void consumeSendMailRequest(MailParamsDTO request);
}
