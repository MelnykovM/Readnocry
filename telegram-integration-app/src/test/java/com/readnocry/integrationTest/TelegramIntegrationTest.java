package com.readnocry.integrationTest;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.MailParamsDTO;
import com.readnocry.dto.WordDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.TelegramAppUser;
import com.readnocry.service.AppUserService;
import com.readnocry.service.TelegramAppUserService;
import com.readnocry.testConfiguration.PostgreSQLTestConfig;
import com.readnocry.testConfiguration.RabbitMqTestConfig;
import com.readnocry.testUtils.TelegramTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.readnocry.RabbitQueue.*;
import static com.readnocry.dto.enums.MailPurpose.TELEGRAM_CONNECTION;
import static com.readnocry.entity.enums.TelegramAppUserState.BASIC_STATE;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {RabbitMqTestConfig.class, PostgreSQLTestConfig.class})
public class TelegramIntegrationTest {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private TelegramAppUserService telegramAppUserService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void happyPathConnectionToAccountTest() throws InterruptedException {
        TelegramTestUtils telegramTestUtils = new TelegramTestUtils(appUserService, telegramAppUserService);
        AppUser appUser = telegramTestUtils.createAppUser("usernamenoconnection");

        Update update = telegramTestUtils.createUpdate("usernamenoconnection", "usernamenoconnection@gmail.com", 1L);

        rabbitTemplate.convertAndSend(TELEGRAM_TEXT_MESSAGE_REQUEST, update);

        SendMessage sendMessage = (SendMessage) rabbitTemplate.receiveAndConvert(TELEGRAM_RESPONSE, 10000);
        MailParamsDTO mailParamsDTO = (MailParamsDTO) rabbitTemplate.receiveAndConvert(SEND_MAIL_REQUEST, 10000);

        Thread.sleep(2000);

        assertTrue(sendMessage.getChatId().equals(update.getMessage().getChatId().toString()));
        assertTrue(sendMessage.getText().equals("An email has been sent to you. Please follow the link in the email to confirm."));

        assertTrue(mailParamsDTO.getMailPurpose().equals(TELEGRAM_CONNECTION));
        assertTrue(mailParamsDTO.getEmailTo().equals("usernamenoconnection@gmail.com"));
    }

    @Test
    public void happyPathChatGptTest() throws InterruptedException {
        TelegramTestUtils telegramTestUtils = new TelegramTestUtils(appUserService, telegramAppUserService);
        AppUser appUser = telegramTestUtils.createAppUser("usernamechatgpt");

        Update update = telegramTestUtils.createUpdate("usernamechatgpt", "chatGPT", 2L);
        TelegramAppUser telegramAppUser = telegramTestUtils.saveTelegramAppUser(update, BASIC_STATE, appUser);

        rabbitTemplate.convertAndSend(TELEGRAM_TEXT_MESSAGE_REQUEST, update);
        Thread.sleep(1000);
        SendMessage sendMessage = (SendMessage) rabbitTemplate.receiveAndConvert(TELEGRAM_RESPONSE, 10000);

        assertTrue(sendMessage.getChatId().equals(update.getMessage().getChatId().toString()));
        assertTrue(sendMessage.getText().equals("Start ChatGPT."));

        update = telegramTestUtils.createUpdate("username", "Chat GPT request text", 2L);
        rabbitTemplate.convertAndSend(TELEGRAM_TEXT_MESSAGE_REQUEST, update);
        Thread.sleep(1000);
        sendMessage = (SendMessage) rabbitTemplate.receiveAndConvert(TELEGRAM_RESPONSE, 10000);
        assertTrue(sendMessage.getChatId().equals(update.getMessage().getChatId().toString()));
        assertTrue(sendMessage.getText().equals("Request was sent"));
        ChatGptInteractionDTO chatGptInteractionDTO = (ChatGptInteractionDTO) rabbitTemplate.receiveAndConvert(CHAT_GPT_REQUEST, 10000);
        assertTrue(chatGptInteractionDTO.getMessage().equals("Chat GPT request text"));
        assertTrue(chatGptInteractionDTO.getTelegramChatId().equals(telegramAppUser.getTelegramChatId()));
        assertTrue(chatGptInteractionDTO.getAppUserId().equals(appUser.getId()));

        chatGptInteractionDTO.setMessage("Chat GPT answer text");
        rabbitTemplate.convertAndSend(CHAT_GPT_RESPONSE, chatGptInteractionDTO);
        Thread.sleep(1000);
        sendMessage = (SendMessage) rabbitTemplate.receiveAndConvert(TELEGRAM_RESPONSE, 10000);
        assertTrue(sendMessage.getChatId().equals(update.getMessage().getChatId().toString()));
        assertTrue(sendMessage.getText().equals("Chat GPT answer text"));
    }

    @Test
    public void happyPathTranslationToTelegramTest() throws InterruptedException {
        TelegramTestUtils telegramTestUtils = new TelegramTestUtils(appUserService, telegramAppUserService);
        AppUser appUser = telegramTestUtils.createAppUser("usernametranslation");

        Update update = telegramTestUtils.createUpdate("usernametranslation", "chatGPT", 3L);
        TelegramAppUser telegramAppUser = telegramTestUtils.saveTelegramAppUser(update, BASIC_STATE, appUser);

        WordDTO wordDTO = new WordDTO(appUser.getId(), "id", "word", "transcription", "translation");

        rabbitTemplate.convertAndSend(TRANSLATION_TO_TELEGRAM_REQUEST, wordDTO);
        Thread.sleep(1000);
        SendMessage sendMessage = (SendMessage) rabbitTemplate.receiveAndConvert(TELEGRAM_DICTIONARY_RESPONSE, 10000);

        assertTrue(sendMessage.getChatId().equals(update.getMessage().getChatId().toString()));
        assertTrue(sendMessage.getText().equals("translation - <tg-spoiler>transcription - word</tg-spoiler>"));
    }
}
