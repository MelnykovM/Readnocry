package com.readnocry.book.service.impl;

import com.readnocry.book.service.BookService;
import com.readnocry.book.service.MappedFileService;
import com.readnocry.entity.BookMetaData;
import com.readnocry.exception.BookNotFoundException;
import com.readnocry.service.BookStockService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Log4j
public class BookServiceImpl implements BookService {

    private final BookStockService bookStockService;
    private final MappedFileService mappedFileService;

    public BookServiceImpl(BookStockService bookStockService, MappedFileService mappedFileService) {
        this.bookStockService = bookStockService;
        this.mappedFileService = mappedFileService;
    }

    @Override
    @Transactional
    public String getPageContent(Long bookMetaDataId, int pageNum) {
        log.info("Get page content from book: " + bookMetaDataId + ", Page: " + pageNum);
        BookMetaData bookMetaData = bookStockService.findById(bookMetaDataId).orElseThrow(() -> new BookNotFoundException("Book was not found for Id = " + bookMetaDataId));
        String pageResult = "";
        try {
            pageResult = mappedFileService.readRandomPage(bookMetaData, pageNum);
            bookMetaData.setPage(pageNum);
            bookStockService.save(bookMetaData);
        } catch (IOException e) {
            log.error("Error get page content from book: " + bookMetaDataId + ", Page: " + pageNum, e);
            pageResult = "Oooops. Something went wrong. =(";
        }
        return pageResult;
    }

    @Override
    @Transactional
    public String getNextPageContent(Long bookMetaDataId) {
        log.info("Get next page content from book: " + bookMetaDataId);
        BookMetaData bookMetaData = bookStockService.findById(bookMetaDataId).orElseThrow(() -> new BookNotFoundException("Book was not found for Id = " + bookMetaDataId));
        String pageResult = "";
        try {
            pageResult = mappedFileService.readNextPage(bookMetaData);
            bookMetaData.setPage(bookMetaData.getPage() + 1);
            bookStockService.save(bookMetaData);
        } catch (IOException e) {
            log.error("Error get next page content from book: " + bookMetaDataId, e);
            pageResult = "Oooops. Something went wrong. =(";
        }
        return pageResult;
    }

    @Override
    @Transactional
    public String getPreviousPageContent(Long bookMetaDataId) {
        log.info("Get previous page content from book: " + bookMetaDataId);
        BookMetaData bookMetaData = bookStockService.findById(bookMetaDataId).orElseThrow(() -> new BookNotFoundException("Book was not found for Id = " + bookMetaDataId));
        String pageResult = "";
        try {
            pageResult = mappedFileService.readPreviousPage(bookMetaData);
            bookMetaData.setPage(bookMetaData.getPage() - 1);
            bookStockService.save(bookMetaData);
        } catch (IOException e) {
            log.error("Error get previous page content from book: " + bookMetaDataId, e);
            pageResult = "Oooops. Something went wrong. =(";
        }
        return pageResult;
    }
}
