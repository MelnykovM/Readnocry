package com.readnocry.telegramIntegration.service.impl;

import com.readnocry.CryptoTool;
import com.readnocry.dto.MailParamsDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.TelegramAppUser;
import com.readnocry.rabbitmq.service.TelegramIntegrationRabbitProducer;
import com.readnocry.service.AppUserService;
import com.readnocry.service.TelegramAppUserService;
import com.readnocry.telegramIntegration.service.TelegramUserAccountService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;

import static com.readnocry.dto.enums.MailPurpose.TELEGRAM_CONNECTION;
import static com.readnocry.entity.enums.TelegramAppUserState.WAIT_FOR_EMAIL_CONFIRMATION_STATE;
import static com.readnocry.entity.enums.TelegramAppUserState.WAIT_FOR_EMAIL_STATE;

@Service
@Log4j
public class TelegramUserAccountServiceImpl implements TelegramUserAccountService {

    private final TelegramIntegrationRabbitProducer telegramIntegrationRabbitProducer;
    private final TelegramAppUserService telegramAppUserService;
    private final AppUserService appUserService;
    private final CryptoTool cryptoTool;


    public TelegramUserAccountServiceImpl(TelegramIntegrationRabbitProducer telegramIntegrationRabbitProducer,
                                          TelegramAppUserService telegramAppUserService,
                                          AppUserService appUserService,
                                          CryptoTool cryptoTool) {
        this.telegramIntegrationRabbitProducer = telegramIntegrationRabbitProducer;
        this.telegramAppUserService = telegramAppUserService;
        this.appUserService = appUserService;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public void processConnectionToAccount(Update update, TelegramAppUser telegramAppUser) {
        log.info("Processing connection to account for telegram user: " + telegramAppUser);
        var email = update.getMessage().getText();
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException e) {
            if (telegramAppUser.getState().equals(WAIT_FOR_EMAIL_CONFIRMATION_STATE)) {
                log.info("Processing connection to account - invalid mail, has already been sent - for telegram user: " + telegramAppUser);
                var answer = String.format("An email has already been sent to %s. " +
                        "Follow the link in the email to confirm. " +
                        "Or write a new email address to restart the connection process.", telegramAppUser.getEmail());
                sendAnswer(answer, telegramAppUser);
                return;
            }
            if (telegramAppUser.getState().equals(WAIT_FOR_EMAIL_STATE) && telegramAppUser.getConnectedToAppUser()) {
                log.info("Processing connection to account - invalid mail, already connected - for telegram user: " + telegramAppUser);
                var answer = String.format("You are in the registration mode. Your telegram account is already connected to account with email %s. " +
                        "Now you can reconnect it to another account. Please enter a new email or enter cancel.", telegramAppUser.getEmail());
                sendAnswerWithBaseKeyboardMarkup(telegramAppUser, answer);
                return;
            }
            log.info("Processing connection to account - invalid mail - for telegram user: " + telegramAppUser);
            var answer = "Firstly you have to connect your telegram to your account. Write your account email.";
            sendAnswer(answer, telegramAppUser);
            return;
        }
        var answer = connectToAccountViaEmail(update, telegramAppUser);
        sendAnswerWithBaseKeyboardMarkup(telegramAppUser, answer);
    }

    @Transactional
    public String connectToAccountViaEmail(Update update, TelegramAppUser telegramAppUser) {
        var email = update.getMessage().getText();
        log.info("Start connecting to telegram account " + telegramAppUser + " via email: " + email);
        var optionalUser = appUserService.findByEmail(email);
        if (optionalUser.isEmpty()) {
            var msg = String.format("User with email %s not found.", email);
            log.error(msg);
            return msg;
        } else {
            telegramAppUser.setEmail(email);
            telegramAppUser.setConnectedToAppUser(false);
            telegramAppUser.setState(WAIT_FOR_EMAIL_CONFIRMATION_STATE);
            AppUser appUser = optionalUser.get();
            telegramAppUserService.saveTelegramAppUser(telegramAppUser);
            sendMailConnection(appUser);
            log.info("Email was sent successfully to " + email);
            return "An email has been sent to you. Please follow the link in the email to confirm.";
        }
    }

    private void sendMailConnection(AppUser appUser) {
        var cryptoUserId = cryptoTool.hashOf(appUser.getId());
        var mailParams = MailParamsDTO.builder()
                .id(cryptoUserId)
                .emailTo(appUser.getEmail())
                .mailPurpose(TELEGRAM_CONNECTION)
                .build();
        telegramIntegrationRabbitProducer.produceSendMailRequest(mailParams);
    }

    @Override
    public void processReconnectionToAccount(TelegramAppUser telegramAppUser) {
        log.info("Processing reconnection to account for telegram user: " + telegramAppUser);
        telegramAppUser.setState(WAIT_FOR_EMAIL_STATE);
        telegramAppUserService.saveTelegramAppUser(telegramAppUser);
        if (telegramAppUser.getConnectedToAppUser()) {
            var answer = String.format("Your telegram account is already connected to account with email %s. " +
                    "Now you can reconnect it to another account. Please enter a new email or enter cancel.", telegramAppUser.getEmail());
            sendAnswerWithBaseKeyboardMarkup(telegramAppUser, answer);
        }
        var answer = "Please enter a new email.";
        sendAnswerWithBaseKeyboardMarkup(telegramAppUser, answer);
    }

    private void sendAnswer(String output, TelegramAppUser telegramAppUser) {
        log.info("Sending simple answer for telegram user: " + telegramAppUser);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(telegramAppUser.getTelegramChatId());
        sendMessage.setText(output);
        telegramIntegrationRabbitProducer.produceTelegramBotResponse(sendMessage);
    }

    private void sendAnswerWithBaseKeyboardMarkup(TelegramAppUser telegramAppUser, String message) {
        log.info("Sending answer with base keyboard to telegram user: " + telegramAppUser);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(telegramAppUser.getTelegramChatId().toString());
        sendMessage.setText(message);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("chatGPT"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("cancel"));
        row2.add(new KeyboardButton("help"));
        row2.add(new KeyboardButton("registration"));
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(keyboardMarkup);
        telegramIntegrationRabbitProducer.produceTelegramBotResponse(sendMessage);
    }
}
