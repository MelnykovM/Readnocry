package com.readnocry.service.impl;

import com.readnocry.dao.TelegramAppUserDao;
import com.readnocry.dao.TelegramMessageDao;
import com.readnocry.entity.TelegramAppUser;
import com.readnocry.entity.TelegramMessage;
import com.readnocry.service.TelegramAppUserService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@Log4j
public class TelegramAppUserServiceImpl implements TelegramAppUserService {

    private final TelegramAppUserDao telegramAppUserDao;
    private final TelegramMessageDao telegramMessageDao;

    public TelegramAppUserServiceImpl(TelegramAppUserDao telegramAppUserDao, TelegramMessageDao telegramMessageDao) {
        this.telegramAppUserDao = telegramAppUserDao;
        this.telegramMessageDao = telegramMessageDao;
    }

    @Override
    public TelegramAppUser findOrSaveTelegramAppUser(TelegramAppUser telegramAppUser) {
        log.info("Find or save telegram user: " + telegramAppUser);
        Optional<TelegramAppUser> persistentTelegramUser = telegramAppUserDao.findByTelegramUserId(telegramAppUser.getTelegramUserId());
        return persistentTelegramUser.orElseGet(() -> telegramAppUserDao.save(telegramAppUser));
    }

    @Override
    public void saveTelegramAppUser(TelegramAppUser telegramAppUser) {
        log.info("Save telegram user: " + telegramAppUser);
        telegramAppUserDao.save(telegramAppUser);
    }

    @Override
    public Optional<TelegramAppUser> findByEmail(String email) {
        log.info("Find by email telegram user: " + email);
        return telegramAppUserDao.findByEmail(email);
    }

    @Override
    public Optional<TelegramAppUser> findByTelegramUserId(Long telegramUserId) {
        log.info("Find by telegram ID: " + telegramUserId);
        return telegramAppUserDao.findById(telegramUserId);
    }

    @Override
    @Transactional
    public void saveTelegramMessage(TelegramMessage message) {
        log.info("Save telegram message: " + message);
        telegramAppUserDao.findByTelegramChatId(message.getTelegramChatId()).ifPresentOrElse(telegramAppUser -> {
            message.setAppUser(telegramAppUser);
            telegramMessageDao.save(message);
            if (telegramAppUser.getMessages() == null) {
                telegramAppUser.setMessages(new ArrayList<>());
                telegramAppUserDao.save(telegramAppUser);
            }
            telegramAppUser.getMessages().add(message);
            telegramAppUserDao.save(telegramAppUser);
        }, () -> log.error("Telegram user not found for chat ID: " + message.getTelegramChatId()));
    }
}
