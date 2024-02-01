package com.readnocry.web.controller;

import com.readnocry.entity.AppUser;
import com.readnocry.service.AppUserService;
import com.readnocry.web.service.WebDictionaryService;
import lombok.extern.log4j.Log4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Log4j
public class DictionaryController {

    private final AppUserService appUserService;
    private final WebDictionaryService webDictionaryService;

    public DictionaryController(AppUserService appUserService,
                                WebDictionaryService webDictionaryService) {
        this.appUserService = appUserService;
        this.webDictionaryService = webDictionaryService;
    }

    @PostMapping("/add-to-dictionary")
    public ResponseEntity<?> addWordToDictionary(@RequestParam String word,
                                                 @RequestParam String transcription,
                                                 @RequestParam String translation) {
        AppUser appUser = appUserService.getAuthorizedUser();
        log.info("Add to dictionary: " + word + " - " + transcription + " - " + translation + ". For user:" + appUser);
        webDictionaryService.addWordToDictionary(appUser, word, transcription, translation);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete-from-dictionary")
    public ResponseEntity<?> deleteFromDictionary(@RequestParam String wordId,
                                                  @RequestParam String word,
                                                  @RequestParam String transcription,
                                                  @RequestParam String translation) {
        AppUser appUser = appUserService.getAuthorizedUser();
        log.info("Delete from dictionary: " + wordId + ". For user:" + appUser);
        webDictionaryService.deleteFromDictionary(appUser, wordId, word, transcription, translation);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-dictionary")
    public String dictionaryPage(Model model) {
        AppUser appUser = appUserService.getAuthorizedUser();
        log.info("Dictionary page for user: " + appUser);
        model.addAttribute("username", appUser.getUsername());
        return "dictionary";
    }

    @GetMapping("/get-my-dictionary")
    public ResponseEntity<?> getDictionary(Model model) {
        AppUser appUser = appUserService.getAuthorizedUser();
        log.info("Get dictionary for user: " + appUser);
        webDictionaryService.getAllDictionary(appUser);
        return ResponseEntity.ok().build();
    }
}
