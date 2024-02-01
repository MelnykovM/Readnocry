package com.readnocry.web.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendTranslationContent(String username, String message) {
        messagingTemplate.convertAndSendToUser(username, "/topic/translationContent", message);
    }

    public void sendDictionaryContent(String username, String message) {
        try {
            Thread.sleep(900);
        } catch (InterruptedException e) {
            log.error(e);
        }
        messagingTemplate.convertAndSendToUser(username, "/topic/dictionaryContent", message);
    }

    public void sendErrorContent(String username, String message) {
        try {
            Thread.sleep(900);
        } catch (InterruptedException e) {
            log.error(e);
        }
        messagingTemplate.convertAndSendToUser(username, "/topic/errorContent", message);
    }

    public void showLoadingAnimation(String username) {
        messagingTemplate.convertAndSendToUser(username, "/topic/control", "{\"action\": \"showLoading\"}");
    }
}
