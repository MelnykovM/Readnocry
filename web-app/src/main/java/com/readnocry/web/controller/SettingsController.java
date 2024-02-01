package com.readnocry.web.controller;

import com.readnocry.entity.AppUser;
import com.readnocry.entity.AppUserSettings;
import com.readnocry.service.AppUserService;
import com.readnocry.service.SettingsService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Log4j
public class SettingsController {

    private final SettingsService settingsService;
    private final AppUserService appUserService;

    public SettingsController(SettingsService settingsService,
                              AppUserService appUserService) {
        this.settingsService = settingsService;
        this.appUserService = appUserService;
    }

    @GetMapping("/settings")
    public String settingsForm(Model model) {
        AppUser appUser = appUserService.getAuthorizedUser();
        model.addAttribute("settings", appUser.getAppUserSettings());
        return "settings";
    }

    @PostMapping("/saveSettings")
    public String saveSettings(AppUserSettings settings) {
        log.info("Save settings: " + settings);
        settingsService.saveSettings(settings);
        return "redirect:/settings";
    }

    @GetMapping("/tokens")
    public String tokensForm(Model model) {
        AppUser appUser = appUserService.getAuthorizedUser();
        model.addAttribute("tokens", appUser.getTokensCount());
        return "tokens";
    }

    @PostMapping("/add-tokens")
    public String addTokens(@RequestParam("addTokens") Integer tokens, Model model) {
        AppUser appUser = appUserService.getAuthorizedUser();
        log.info("Add tokens: " + tokens + " to user: " + appUser);
        appUserService.addTokens(appUser, tokens);
        return "redirect:/bookshelf";
    }
}
