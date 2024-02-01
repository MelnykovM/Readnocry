package com.readnocry.web.controller;

import com.readnocry.service.AppUserService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Log4j
public class LoginController {

    private final AppUserService appUserService;

    public LoginController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        log.info("Login user.");
        if (error != null) {
            log.warn("Your username and password are invalid.");
            model.addAttribute("errorMsg", "Your username and password are invalid.");
        }
        if (logout != null) {
            model.addAttribute("msg", "You have been logged out successfully.");
        } else if (appUserService.getCurrentUser() != null) {
            log.info("User already logged in.");
            return "redirect:/bookshelf";
        }
        return "login";
    }
}

