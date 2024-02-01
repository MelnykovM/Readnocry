package com.readnocry.telegram.service;

import com.readnocry.telegram.controller.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramBotService {

    void registerBot(TelegramBot telegramBot);

    void processUpdate(Update update);

    void sendMessage(SendMessage sendMessage);

    void sendDictionaryMessage(SendMessage sendMessage);

    void saveMessage(Message message);

    void deleteMessage(DeleteMessage deleteMessage);
}
