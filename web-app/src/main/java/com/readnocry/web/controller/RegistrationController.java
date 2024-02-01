package com.readnocry.web.controller;

import com.readnocry.dto.AppUserDTO;
import com.readnocry.exception.EmailExistsException;
import com.readnocry.exception.UsernameExistsException;
import com.readnocry.service.AppUserService;
import com.readnocry.web.service.AppUserAccountService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@Log4j
public class RegistrationController {

    private final AppUserService appUserService;
    private final AppUserAccountService appUserAccountService;

    public RegistrationController(AppUserService appUserService, AppUserAccountService appUserAccountService) {
        this.appUserService = appUserService;
        this.appUserAccountService = appUserAccountService;
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {
        if (appUserService.getCurrentUser() != null) {
            log.info("User already logged in.");
            return "redirect:/bookshelf";
        }
        model.addAttribute("appUserDTO", new AppUserDTO());
        return "registration";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute @Valid AppUserDTO appUserDTO,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            log.error("Email invalid");
            return "registration";
        }
        try {
            appUserAccountService.createAppUserAccount(appUserDTO);
        } catch (EmailExistsException e) {
            log.error("Email already exists", e);
            model.addAttribute("emailExists", "Email already exists");
            return "registration";
        } catch (UsernameExistsException e) {
            log.error("Username already exists", e);
            model.addAttribute("usernameExists", "Username already exists");
            return "registration";
        }
        return "redirect:/login";
    }
}
