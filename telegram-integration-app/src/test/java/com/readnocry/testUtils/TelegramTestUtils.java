package com.readnocry.testUtils;

import com.readnocry.dto.AppUserDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.TelegramAppUser;
import com.readnocry.entity.enums.TelegramAppUserState;
import com.readnocry.service.AppUserService;
import com.readnocry.service.TelegramAppUserService;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Random;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

public class TelegramTestUtils {

    private final AppUserService appUserService;
    private final TelegramAppUserService telegramAppUserService;

    public TelegramTestUtils(AppUserService appUserService, TelegramAppUserService telegramAppUserService) {
        this.appUserService = appUserService;
        this.telegramAppUserService = telegramAppUserService;
    }

    @Transactional(propagation = REQUIRES_NEW)
    public AppUser createAppUser(String username) {
        AppUserDTO appUserDTO = new AppUserDTO(username, "pass", username + "@gmail.com");
        AppUser appUser = appUserService.createNewAppUser(appUserDTO);
        return appUserService.findByUsername(appUser.getUsername()).orElseThrow();
    }

    @Transactional(propagation = REQUIRES_NEW)
    public TelegramAppUser saveTelegramAppUser(Update update,
                                               TelegramAppUserState telegramAppUserState,
                                               AppUser appUser) {
        User telegramUser = update.getMessage().getFrom();
        TelegramAppUser transientTelegramAppUser = TelegramAppUser.builder()
                .telegramUserId(telegramUser.getId())
                .telegramChatId(update.getMessage().getChatId())
                .username(telegramUser.getUserName())
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .state(telegramAppUserState)
                .connectedToAppUser(true)
                .email(appUser.getEmail())
                .build();
        TelegramAppUser telegramAppUser = telegramAppUserService.findOrSaveTelegramAppUser(transientTelegramAppUser);

        appUser.setTelegramUserId(telegramAppUser.getTelegramUserId());
        appUser.setTelegramChatId(telegramAppUser.getTelegramChatId());
        appUser.setFirstName(telegramAppUser.getFirstName());
        appUser.setLastName(telegramAppUser.getLastName());
        appUser.setEmail(telegramAppUser.getEmail());

        appUserService.saveAppUser(appUser);

        return telegramAppUser;
    }

    public Update createUpdate(String username, String textMessage, Long id) {
        User telegramUser = new User();
        telegramUser.setId(id);
        telegramUser.setUserName(username);
        telegramUser.setFirstName("First name");
        telegramUser.setFirstName("Last name");
        Chat chat = new Chat();
        chat.setId(id);
        Message message = new Message();
        message.setText(textMessage);
        message.setMessageId(new Random().nextInt());
        message.setChat(chat);
        message.setFrom(telegramUser);
        Update update = new Update();
        update.setMessage(message);
        return update;
    }
}
