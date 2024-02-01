package com.readnocry.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.BookMetaData;
import com.readnocry.service.AppUserService;
import com.readnocry.service.BookStockService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@Log4j
public class ReaderController {

    private final AppUserService appUserService;
    private final ObjectMapper objectMapper;
    private final BookStockService bookStockService;

    public ReaderController(AppUserService appUserService,
                            ObjectMapper objectMapper,
                            BookStockService bookStockService) {
        this.appUserService = appUserService;
        this.objectMapper = objectMapper;
        this.bookStockService = bookStockService;
    }

    @GetMapping("/reader")
    public String viewBookPage(@RequestParam Long bookId, Model model) {
        log.info("Read book: " + bookId);
        Optional<BookMetaData> bookMetaDataOptional = bookStockService.findById(bookId);
        if (bookMetaDataOptional.isEmpty()) {
            log.error("Book not found: " + bookId);
            return "redirect:/bookshelf";
        }
        AppUser appUser = appUserService.getAuthorizedUser();
        BookMetaData bookMetaData = bookMetaDataOptional.get();
        if (!bookStockService.isUserOwnerOfBook(appUser, bookMetaData)) {
            return "redirect:/bookshelf";
        }
        String metaDataJson = getBookMetaDataJson(bookMetaData, bookId);
        model.addAttribute("bookMetaDataJson", metaDataJson);
        model.addAttribute("username", appUser.getUsername());
        return "bookPage";
    }

    private String getBookMetaDataJson(BookMetaData bookMetaData, Long bookId) {
        try {
            return objectMapper.writeValueAsString(bookMetaData);
        } catch (JsonProcessingException e) {
            log.error("Error parsing page for book: " + bookId, e);
            return "{}";
        }
    }
}
