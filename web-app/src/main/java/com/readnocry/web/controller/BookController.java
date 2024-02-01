package com.readnocry.web.controller;

import com.readnocry.book.service.BookService;
import lombok.extern.log4j.Log4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/book")
@Log4j
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }


    @GetMapping("/page")
    public String getPage(@RequestParam Long bookMetaDataId, @RequestParam int page) {
        log.info("Get page from book: " + bookMetaDataId + ". Page: " + page);
        return bookService.getPageContent(bookMetaDataId, page);
    }

    @GetMapping("/next-page")
    public String getNextPage(@RequestParam Long bookMetaDataId) {
        log.info("Get next page from book: " + bookMetaDataId);
        return bookService.getNextPageContent(bookMetaDataId);
    }

    @GetMapping("/previous-page")
    public String getPreviousPage(@RequestParam Long bookMetaDataId) {
        log.info("Get previous page from book: " + bookMetaDataId);
        return bookService.getPreviousPageContent(bookMetaDataId);
    }
}
