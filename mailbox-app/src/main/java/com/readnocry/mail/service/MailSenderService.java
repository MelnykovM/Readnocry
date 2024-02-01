package com.readnocry.mail.service;

import com.readnocry.dto.MailParamsDTO;

public interface MailSenderService {

    void processSendMailRequest(MailParamsDTO request);
}
