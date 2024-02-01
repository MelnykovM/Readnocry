package com.readnocry.telegramIntegration.service.impl;

import com.readnocry.dao.TelegramUpdateRawDataDao;
import com.readnocry.dto.WordDTO;
import com.readnocry.entity.TelegramAppUser;
import com.readnocry.entity.TelegramMessage;
import com.readnocry.entity.TelegramUpdateRawData;
import com.readnocry.rabbitmq.service.TelegramIntegrationRabbitProducer;
import com.readnocry.service.AppUserService;
import com.readnocry.service.TelegramAppUserService;
import com.readnocry.telegramIntegration.service.ChatGptService;
import com.readnocry.telegramIntegration.service.TelegramIntegrationService;
import com.readnocry.telegramIntegration.service.TelegramUserAccountService;
import com.readnocry.telegramIntegration.service.enums.ServiceCommand;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.readnocry.entity.enums.TelegramAppUserState.*;

@Service
@Log4j
public class TelegramIntegrationServiceImpl implements TelegramIntegrationService {

    private final TelegramUpdateRawDataDao telegramUpdateRawDataDao;
    private final TelegramIntegrationRabbitProducer telegramIntegrationRabbitProducer;
    private final AppUserService appUserService;
    private final ChatGptService chatGptService;
    private final TelegramAppUserService telegramAppUserService;
    private final TelegramUserAccountService telegramUserAccountService;

    public TelegramIntegrationServiceImpl(TelegramUpdateRawDataDao telegramUpdateRawDataDao,
                                          TelegramIntegrationRabbitProducer telegramIntegrationRabbitProducer,
                                          AppUserService appUserService,
                                          ChatGptService chatGptService,
                                          TelegramAppUserService telegramAppUserService, TelegramUserAccountService telegramUserAccountService) {
        this.telegramUpdateRawDataDao = telegramUpdateRawDataDao;
        this.telegramIntegrationRabbitProducer = telegramIntegrationRabbitProducer;
        this.appUserService = appUserService;
        this.chatGptService = chatGptService;
        this.telegramAppUserService = telegramAppUserService;
        this.telegramUserAccountService = telegramUserAccountService;
    }

    @Override
    @Transactional
    public void processTextMessage(Update update) {
        log.info("Starting process test message: " + update.getMessage().getText() + ". For telegram user: " + update.getMessage().getFrom().getUserName());
        saveUpdateRawData(update);
        var text = update.getMessage().getText();
        TelegramAppUser telegramAppUser = findOrSaveTelegramAppUser(update);
        deleteAllMessages(telegramAppUser);
        saveTelegramMessage(TelegramMessage.builder()
                .telegramMessageId(update.getMessage().getMessageId())
                .telegramChatId(update.getMessage().getChatId())
                .build());

        var telegramAppUserState = telegramAppUser.getState();
        if (ServiceCommand.CANCEL.equals(ServiceCommand.fromValue(update.getMessage().getText())) &&
                telegramAppUser.getConnectedToAppUser()) {
            cancelProcess(telegramAppUser);
        } else if (telegramAppUser.getState().equals(WAIT_FOR_EMAIL_STATE) ||
                telegramAppUser.getState().equals(WAIT_FOR_EMAIL_CONFIRMATION_STATE)) {
            telegramUserAccountService.processConnectionToAccount(update, telegramAppUser);
        } else {
            if (BASIC_STATE.equals(telegramAppUserState)) {
                processServiceCommand(telegramAppUser, text);
            } else if (CHAT_STATE.equals(telegramAppUserState)) {
                chatGptService.chatGptProcessRequest(update, text);
            } else {
                log.error("Unknown user state: " + telegramAppUserState);
                var output = "Unknown error! Type cancel and try again!";
                sendAnswerWithBaseKeyboardMarkup(telegramAppUser, output);
            }
        }
    }

    private TelegramAppUser findOrSaveTelegramAppUser(Update update) {
        log.info("Find or create new telegram user: " + update.getMessage().getFrom().getUserName());
        User telegramUser = update.getMessage().getFrom();
        TelegramAppUser transientTelegramAppUser = TelegramAppUser.builder()
                .telegramUserId(telegramUser.getId())
                .telegramChatId(update.getMessage().getChatId())
                .username(telegramUser.getUserName())
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .state(WAIT_FOR_EMAIL_STATE)
                .connectedToAppUser(false)
                .build();
        return telegramAppUserService.findOrSaveTelegramAppUser(transientTelegramAppUser);
    }

    private void processServiceCommand(TelegramAppUser telegramAppUser, String cmd) {
        log.info("Processing service command for telegram user: " + telegramAppUser);
        var serviceCommand = ServiceCommand.fromValue(cmd);
        if (ServiceCommand.REGISTRATION.equals(serviceCommand)) {
            telegramUserAccountService.processReconnectionToAccount(telegramAppUser);
        } else if (ServiceCommand.HELP.equals(serviceCommand)) {
            help(telegramAppUser);
        } else if (ServiceCommand.CHAT.equals(serviceCommand)) {
            setChatState(telegramAppUser);
        } else if (ServiceCommand.START.equals(serviceCommand)) {
            sendAnswerWithBaseKeyboardMarkup(telegramAppUser, "Welcome!");
        } else {
            log.info("Unknown command!: " + cmd);
            var answer = "Unknown command! To see a list of available commands, enter help";
            sendAnswerWithBaseKeyboardMarkup(telegramAppUser, answer);
        }
    }

    @Override
    public void processTranslationToTelegramRequest(WordDTO request) {
        log.info("Processing translation request: " + request);
        appUserService.findById(request.getAppUserId()).ifPresentOrElse(appUser -> {
            String text = request.getTranslation() + " - " + "<tg-spoiler>" + request.getTranscription() + " - " + request.getWord() + "</tg-spoiler>";
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(appUser.getTelegramChatId());
            sendMessage.enableHtml(true);
            sendMessage.setText(text);
            telegramIntegrationRabbitProducer.produceTelegramDictionaryResponse(sendMessage);
        }, () -> log.error("AppUser not found: " + request));
    }

    @Override
    public void saveTelegramMessage(TelegramMessage request) {
        log.info("Saving telegram message: " + request);
        telegramAppUserService.saveTelegramMessage(request);
    }

    private void sendAnswer(String output, TelegramAppUser telegramAppUser) {
        log.info("Sending simple answer for telegram user: " + telegramAppUser);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(telegramAppUser.getTelegramChatId());
        sendMessage.setText(output);
        telegramIntegrationRabbitProducer.produceTelegramBotResponse(sendMessage);
    }

    private void help(TelegramAppUser telegramAppUser) {
        log.info("Help menu for telegram user: " + telegramAppUser);
        var answer = "List of available commands:\n"
                + "help - this menu;\n"
                + "cancel - cancel the current command;\n"
                + "registration - user re-registration.\n"
                + "chatGPT - start chatting with ChatGPT.\n"
                + "show history - show chat history with ChatGPT (in ChatGPT mode).\n"
                + "clean history - clear chat context (in ChatGPT mode).\n"
                + "setup not save history - ChatGPT will not remember context (in ChatGPT mode).\n"
                + "setup save history - ChatGPT will remember context, but you will spend more tokens (in ChatGPT mode).";
        sendAnswerWithBaseKeyboardMarkup(telegramAppUser, answer);
    }

    private void cancelProcess(TelegramAppUser telegramAppUser) {
        log.info("Setting basic state for telegram user: " + telegramAppUser);
        telegramAppUser.setState(BASIC_STATE);
        telegramAppUserService.saveTelegramAppUser(telegramAppUser);
        sendAnswerWithBaseKeyboardMarkup(telegramAppUser, "You are in a base mode.");
    }

    private void setChatState(TelegramAppUser telegramAppUser) {
        log.info("Setting chatGPT state for telegram user: " + telegramAppUser);
        telegramAppUser.setState(CHAT_STATE);
        telegramAppUserService.saveTelegramAppUser(telegramAppUser);
        chatGptService.sendAnswerWithChatGptKeyboardMarkup(telegramAppUser.getTelegramChatId(), "Start ChatGPT.");
    }

    private void saveUpdateRawData(Update update) {
        log.info("Saving update raw data for telegram user chat ID: " + update.getMessage().getChatId());
        TelegramUpdateRawData telegramUpdateRawData = TelegramUpdateRawData.builder()
                .event(update)
                .build();
        telegramUpdateRawDataDao.save(telegramUpdateRawData);
    }

    public void deleteAllMessages(TelegramAppUser telegramAppUser) {
        log.info("Deleting saved messages for telegram user: " + telegramAppUser);
        List<TelegramMessage> messages = telegramAppUser.getMessages();
        if (messages != null) {
            messages.forEach(message -> deleteMessage(message.getTelegramChatId(), message.getTelegramMessageId()));
            telegramAppUser.getMessages().clear();
        }
        telegramAppUserService.saveTelegramAppUser(telegramAppUser);
    }

    public void deleteMessage(Long chatId, Integer messageId) {
        log.info("Deleting message: " + messageId);
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId.toString());
        deleteMessage.setMessageId(messageId);
        telegramIntegrationRabbitProducer.produceDeleteTelegramBotMessageRequest(deleteMessage);
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
