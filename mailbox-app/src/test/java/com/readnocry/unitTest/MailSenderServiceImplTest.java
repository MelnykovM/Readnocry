package com.readnocry.unitTest;

import com.readnocry.dto.MailParamsDTO;
import com.readnocry.dto.enums.MailPurpose;
import com.readnocry.mail.service.impl.MailSenderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class MailSenderServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    private MailSenderServiceImpl mailSenderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mailSenderService = new MailSenderServiceImpl(javaMailSender);
    }

    @Test
    void testProcessSendMailRequestTelegramConnection() {
        MailParamsDTO request = new MailParamsDTO();
        request.setMailPurpose(MailPurpose.TELEGRAM_CONNECTION);
        request.setEmailTo("test@example.com");
        request.setId("testid");

        mailSenderService.processSendMailRequest(request);

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testProcessSendMailRequestAccountActivation() {
        MailParamsDTO request = new MailParamsDTO();
        request.setMailPurpose(MailPurpose.ACCOUNT_ACTIVATION);
        request.setEmailTo("test@example.com");
        request.setId("testid");

        mailSenderService.processSendMailRequest(request);

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testProcessSendMailRequestMailException() {
        MailParamsDTO request = new MailParamsDTO();
        request.setMailPurpose(MailPurpose.ACCOUNT_ACTIVATION);
        request.setEmailTo("test@example.com");
        request.setId("testid");

        doThrow(new MailSendException("Mail sending error") {
        }).when(javaMailSender).send(any(SimpleMailMessage.class));

        mailSenderService.processSendMailRequest(request);
    }

    @Test
    void testProcessSendMailRequestIdIsNull() {
        MailParamsDTO request = new MailParamsDTO();
        request.setMailPurpose(MailPurpose.TELEGRAM_CONNECTION);
        request.setEmailTo("test@example.com");
        request.setId(null);

        mailSenderService.processSendMailRequest(request);
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testProcessSendMailRequestInvalidMailPurpose() {
        MailParamsDTO request = new MailParamsDTO();
        request.setMailPurpose(null);
        request.setEmailTo("test@example.com");
        request.setId("testid");

        mailSenderService.processSendMailRequest(request);
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }
}
