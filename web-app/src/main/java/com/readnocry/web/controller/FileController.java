package com.readnocry.web.controller;

import com.readnocry.entity.AppUser;
import com.readnocry.service.AppUserService;
import com.readnocry.service.BookStockService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@Log4j
public class FileController {

    private final BookStockService bookStockService;
    private final AppUserService appUserService;

    public FileController(BookStockService bookStockService, AppUserService appUserService) {
        this.bookStockService = bookStockService;
        this.appUserService = appUserService;
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        return "uploadForm";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("bookTitle") String bookTitle) {
        try {
            log.info("Upload new book: " + file.getOriginalFilename());
            AppUser appUser = appUserService.getAuthorizedUser();
            bookStockService.saveFileOnDisk(file, appUser, bookTitle);
            return "redirect:/bookshelf";
        } catch (Exception e) {
            log.error("Error uploading new book.", e);
            return "redirect:/uploadError";
        }
    }

    @PostMapping("/delete")
    public String deleteFile(@RequestParam("bookId") Long bookId) {
        log.info("Delete book: " + bookId);
        AppUser appUser = appUserService.getAuthorizedUser();
        bookStockService.delete(appUser, bookId);
        return "redirect:/bookshelf";
    }

    @GetMapping("/uploadError")
    public String uploadError(Model model) {
        return "uploadError";
    }
}
