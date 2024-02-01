package com.readnocry.telegramIntegration.service;

import com.readnocry.dto.WordDTO;
import com.readnocry.entity.TelegramMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramIntegrationService {

    void processTextMessage(Update update);

    void processTranslationToTelegramRequest(WordDTO request);

    void saveTelegramMessage(TelegramMessage request);
}
