package com.readnocry.web.controller;

import com.readnocry.entity.AppUser;
import com.readnocry.service.AppUserService;
import com.readnocry.web.service.AppUserAccountService;
import lombok.extern.log4j.Log4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/command")
@Log4j
public class CommandController {

    private final AppUserService appUserService;
    private final AppUserAccountService appUserAccountService;

    public CommandController(AppUserService appUserService, AppUserAccountService appUserAccountService) {
        this.appUserService = appUserService;
        this.appUserAccountService = appUserAccountService;
    }

    @GetMapping("/send-me-mail-again")
    public HttpStatus sendMeMailAgain(Model model) {
        AppUser appUser = appUserService.getAuthorizedUser();
        log.info("Send me mail again for user " + appUser);
        appUserAccountService.processSendMeMailsAgainCommand(appUser);
        return HttpStatus.OK;
    }
}
