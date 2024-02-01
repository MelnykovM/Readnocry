package com.readnocry.mail.service.impl;

import com.readnocry.dto.MailParamsDTO;
import com.readnocry.mail.service.MailSenderService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Log4j
public class MailSenderServiceImpl implements MailSenderService {

    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${service.mail.mail-activation-uri}")
    private String mailActivationServiceUri;
    @Value("${service.mail.telegram-connection-uri}")
    private String mailTelegramConnectionUri;

    public MailSenderServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void processSendMailRequest(MailParamsDTO request) {
        try {
            switch (request.getMailPurpose()) {
                case TELEGRAM_CONNECTION:
                    sendMail(request, mailTelegramConnectionUri);
                    break;
                case ACCOUNT_ACTIVATION:
                    sendMail(request, mailActivationServiceUri);
                    break;
            }
        } catch (MailException e) {
            log.error("Error while sending activation mail: " + request + " - " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: " + request + " - " + e.getMessage());
        }
    }

    private void sendMail(MailParamsDTO request, String mailUri) {
        var subject = request.getMailPurpose().toString();
        var messageBody = getMailBody(request.getId(), mailUri);
        var emailTo = request.getEmailTo();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(messageBody);
        log.info("Sending mail: " + mailMessage);

        javaMailSender.send(mailMessage);
    }

    private String getMailBody(String id, String uri) {
        var msg = String.format("Follow the link to complete:\n%s",
                uri);
        return msg.replace("{id}", id);
    }
}
