package com.readnocry.telegramIntegration.service.impl;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.enums.ChatGptCommand;
import com.readnocry.entity.AppUser;
import com.readnocry.rabbitmq.service.TelegramIntegrationRabbitProducer;
import com.readnocry.service.AppUserService;
import com.readnocry.telegramIntegration.service.ChatGptService;
import com.readnocry.telegramIntegration.service.enums.ServiceCommand;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.readnocry.dto.enums.ChatGptCommand.CLEAN_CHAT_GPT_HISTORY;
import static com.readnocry.dto.enums.ChatGptCommand.SHOW_CHAT_GPT_HISTORY;

@Service
@Log4j
public class ChatGptServiceImpl implements ChatGptService {

    private final TelegramIntegrationRabbitProducer telegramIntegrationRabbitProducer;
    private final AppUserService appUserService;

    public ChatGptServiceImpl(TelegramIntegrationRabbitProducer telegramIntegrationRabbitProducer,
                              AppUserService appUserService) {
        this.telegramIntegrationRabbitProducer = telegramIntegrationRabbitProducer;
        this.appUserService = appUserService;
    }

    @Override
    public void chatGptRequest(ChatGptInteractionDTO request) {
        telegramIntegrationRabbitProducer.produceChatGptRequest(request);
    }

    @Override
    public void processChatGptResponse(ChatGptInteractionDTO response) {
        sendAnswerWithChatGptKeyboardMarkup(response.getTelegramChatId(), response.getMessage());
    }

    @Override
    public void chatGptProcessRequest(Update update, String cmd) {
        log.info("Starting process chatGPT request for telegram user: " + update.getMessage().getFrom().getUserName());
        appUserService.findAppUserByTelegramUserId(update.getMessage().getFrom().getId())
                .ifPresentOrElse(appUser -> {
                    String commandResult = processIfChatCommand(cmd, appUser);
                    if (commandResult == null) {
                        chatGptRequest(createChatGptInteraction(update, appUser));
                        sendAnswerWithChatGptKeyboardMarkup(update.getMessage().getChatId(), "Request was sent");
                    } else {
                        sendAnswerWithChatGptKeyboardMarkup(update.getMessage().getChatId(), commandResult);
                    }
                }, () -> {
                    log.error("Can not find the user connected to telegram user: " + update.getMessage().getFrom().getUserName());
                    var answer = "Can not find the user connected to telegram! Type cancel and try again!";
                    sendAnswerWithChatGptKeyboardMarkup(update.getMessage().getChatId(), answer);
                });
    }

    private String processIfChatCommand(String cmd, AppUser appUser) {
        log.info("Processing chatGPT command for user: " + appUser);
        var serviceCommand = ServiceCommand.fromValue(cmd);
        if (ServiceCommand.CLEAN_HISTORY.equals(serviceCommand)) {
            chatGptRequest(createChatGptInteractionCommand(CLEAN_CHAT_GPT_HISTORY, appUser));
            return "Cleaning the history";
        } else if (ServiceCommand.SHOW_HISTORY.equals(serviceCommand)) {
            chatGptRequest(createChatGptInteractionCommand(SHOW_CHAT_GPT_HISTORY, appUser));
            return "Preparing the history";
        } else if (ServiceCommand.SETUP_NOT_SAVE_HISTORY.equals(serviceCommand) || ServiceCommand.SETUP_SAVE_HISTORY.equals(serviceCommand)) {
            return setupSettings(appUser, serviceCommand);
        } else {
            return null;
        }
    }

    private ChatGptInteractionDTO createChatGptInteraction(Update update, AppUser appUser) {
        return ChatGptInteractionDTO.builder()
                .appUserId(appUser.getId())
                .telegramChatId(appUser.getTelegramChatId())
                .isCommand(false)
                .message(update.getMessage().getText())
                .build();
    }

    private ChatGptInteractionDTO createChatGptInteractionCommand(ChatGptCommand commandName,
                                                                  AppUser appUser) {
        return ChatGptInteractionDTO.builder()
                .appUserId(appUser.getId())
                .telegramChatId(appUser.getTelegramChatId())
                .isCommand(true)
                .command(commandName)
                .build();
    }

    @Override
    public void sendAnswerWithChatGptKeyboardMarkup(Long chatId, String message) {
        log.info("Sending answer with chatGpt keyboard to telegram user chat ID: " + chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("show history"));
        row1.add(new KeyboardButton("clean history"));
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("setup not save history"));
        row2.add(new KeyboardButton("setup save history"));
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("cancel"));
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(keyboardMarkup);
        telegramIntegrationRabbitProducer.produceTelegramBotResponse(sendMessage);
    }

    private String setupSettings(AppUser appUser, ServiceCommand command) {
        if (ServiceCommand.SETUP_NOT_SAVE_HISTORY.equals(command)) {
            appUser.setSaveChatGptHistory(false);
            appUserService.saveAppUser(appUser);
            return "Now your chat context will not be saved.";
        } else if (ServiceCommand.SETUP_SAVE_HISTORY.equals(command)) {
            appUser.setSaveChatGptHistory(true);
            appUserService.saveAppUser(appUser);
            return "Now your chat context will be saved (increased consumption of tokens).";
        } else {
            log.error("Unknown error with chatGPT command: " + command);
            return "Unknown error! Type cancel and try again!";
        }
    }
}
