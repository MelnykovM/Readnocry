package com.readnocry.web.controller;

import com.readnocry.web.service.AppUserAccountService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/user")
@Controller
@Log4j
public class ActivationController {
    
    private final AppUserAccountService appUserAccountService;

    public ActivationController(AppUserAccountService appUserAccountService) {
        this.appUserAccountService = appUserAccountService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/mail-activation")
    public String mailActivation(@RequestParam("id") String id) {
        log.info("Mail activation: " + id);
        appUserAccountService.mailActivation(id);
        return "accountActivationSuccess";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/telegram-connection")
    public String telegramConnection(@RequestParam("id") String id) {
        log.info("Mail telegram connection: " + id);
        appUserAccountService.telegramConnection(id);
        return "telegramActivationSuccess";
    }
}
