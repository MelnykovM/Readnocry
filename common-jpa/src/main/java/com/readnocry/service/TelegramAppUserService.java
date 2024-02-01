package com.readnocry.service;

import com.readnocry.entity.TelegramAppUser;
import com.readnocry.entity.TelegramMessage;

import java.util.Optional;

public interface TelegramAppUserService {

    TelegramAppUser findOrSaveTelegramAppUser(TelegramAppUser telegramAppUser);

    void saveTelegramAppUser(TelegramAppUser telegramAppUser);

    Optional<TelegramAppUser> findByEmail(String email);

    Optional<TelegramAppUser> findByTelegramUserId(Long telegramUserId);

    void saveTelegramMessage(TelegramMessage message);
}
