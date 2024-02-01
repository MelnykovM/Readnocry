package com.readnocry.integrationTest;

import com.readnocry.dto.MailParamsDTO;
import com.readnocry.dto.enums.MailPurpose;
import com.readnocry.testConfiguration.RabbitMqTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.readnocry.RabbitQueue.SEND_MAIL_REQUEST;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RabbitMqTestConfig.class})
public class MailboxIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${service.mail.mail-activation-uri}")
    private String mailActivationServiceUri;
    @Value("${service.mail.telegram-connection-uri}")
    private String mailTelegramConnectionUri;

    @Test
    public void happyPathTest() throws InterruptedException {
        MailParamsDTO mailParamsDTO = new MailParamsDTO("id", "user@gmail.com", MailPurpose.ACCOUNT_ACTIVATION);
        rabbitTemplate.convertAndSend(SEND_MAIL_REQUEST, mailParamsDTO);

        Thread.sleep(2000);

        var subject = mailParamsDTO.getMailPurpose().toString();
        var messageBody = getMailBody(mailParamsDTO.getId(), mailActivationServiceUri);
        var emailTo = mailParamsDTO.getEmailTo();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(messageBody);

        verify(javaMailSender, times(1)).send(mailMessage);

        Thread.sleep(2000);

        mailParamsDTO = new MailParamsDTO("id", "user@gmail.com", MailPurpose.TELEGRAM_CONNECTION);

        subject = mailParamsDTO.getMailPurpose().toString();
        messageBody = getMailBody(mailParamsDTO.getId(), mailTelegramConnectionUri);

        mailMessage.setSubject(subject);
        mailMessage.setText(messageBody);

        rabbitTemplate.convertAndSend(SEND_MAIL_REQUEST, mailParamsDTO);

        Thread.sleep(2000);

        verify(javaMailSender, times(1)).send(mailMessage);
    }

    private String getMailBody(String id, String uri) {
        var msg = String.format("Follow the link to complete:\n%s",
                uri);
        return msg.replace("{id}", id);
    }
}
