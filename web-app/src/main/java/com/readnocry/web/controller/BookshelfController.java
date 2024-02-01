package com.readnocry.web.controller;

import com.readnocry.entity.AppUser;
import com.readnocry.entity.BookMetaData;
import com.readnocry.service.AppUserService;
import com.readnocry.service.TelegramAppUserService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
@Log4j
public class BookshelfController {

    private final AppUserService appUserService;
    private final TelegramAppUserService telegramAppUserService;

    public BookshelfController(AppUserService appUserService,
                               TelegramAppUserService telegramAppUserService) {
        this.appUserService = appUserService;
        this.telegramAppUserService = telegramAppUserService;
    }

    @GetMapping("/bookshelf")
    @Transactional
    public String viewBookMetaDataList(Model model) {
        AppUser appUser = appUserService.getAuthorizedUser();
        log.info("Get all books for user " + appUser);
        Set<BookMetaData> bookSet = appUser.getBooks();

        var telegramUserPresent = telegramAppUserService.findByEmail(appUser.getEmail()).isPresent();
        var tokensBalance = appUser.getTokensCount() < 1000
                ? String.valueOf(appUser.getTokensCount())
                : (appUser.getTokensCount() / 1000) + "k";

        model.addAttribute("books", bookSet);
        model.addAttribute("tokensCount", tokensBalance);
        model.addAttribute("activated", appUser.getIsActive());
        model.addAttribute("telegramMailIsWaiting", (appUser.getTelegramUserId() == null && telegramUserPresent));
        model.addAttribute("telegramShouldBeConnected", !telegramUserPresent);
        log.info("Get all books result: " + appUser + ". Books: " + bookSet + ". Tokens: " + tokensBalance);
        return "bookshelf";
    }
}
