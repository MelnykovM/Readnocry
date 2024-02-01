package com.readnocry.web.controller;

import com.readnocry.service.AppUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AppUserService appUserService;

    public HomeController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        if (appUserService.getCurrentUser() != null) {
            return "aboutApp";
        }
        return "about";
    }
}
