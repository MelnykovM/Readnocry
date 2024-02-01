package com.readnocry.telegramIntegration.service;

import com.readnocry.entity.TelegramAppUser;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramUserAccountService {

    void processConnectionToAccount(Update update, TelegramAppUser telegramAppUser);

    void processReconnectionToAccount(TelegramAppUser telegramAppUser);
}
