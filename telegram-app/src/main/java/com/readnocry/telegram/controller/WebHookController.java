package com.readnocry.telegram.controller;

import com.readnocry.telegram.service.TelegramBotService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@Log4j
public class WebHookController {

    private final TelegramBotService telegramBotService;

    @Value("${bot.token}")
    private String botToken;

    public WebHookController(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @RequestMapping(value = "/callback/update", method = RequestMethod.POST)
    public ResponseEntity<?> onUpdateReceived(@RequestBody Update update) {
        log.info("onUpdateReceived " + update);
        telegramBotService.processUpdate(update);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("healthCheck ok");
        return ResponseEntity.ok("Healthy");
    }
}
