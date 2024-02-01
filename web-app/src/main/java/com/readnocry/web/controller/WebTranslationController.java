package com.readnocry.web.controller;

import com.readnocry.dto.WebTranslationProcessingDTO;
import com.readnocry.service.AppUserService;
import com.readnocry.web.service.WebTranslationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translation")
public class WebTranslationController {

    private final WebTranslationService webTranslationService;
    private final AppUserService appUserService;

    public WebTranslationController(WebTranslationService webTranslationService,
                                    AppUserService appUserService) {
        this.webTranslationService = webTranslationService;
        this.appUserService = appUserService;
    }

    @PostMapping("/process-sentence")
    public ResponseEntity<String> processSentence(@RequestBody WebTranslationProcessingDTO request) {
        request.setAppUserId(appUserService.getAuthorizedUser().getId());
        request.setUsername(appUserService.getAuthorizedUser().getUsername());
        webTranslationService.sendRequestForTranslation(request);
        return ResponseEntity.ok("The sentence has been sent for processing: " + request.getSentence());
    }
}
