package com.readnocry.service.impl;

import com.readnocry.dao.AppUserDao;
import com.readnocry.dao.BookMetaDataDao;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.BookMetaData;
import com.readnocry.entity.enums.PageSize;
import com.readnocry.service.BookStockService;
import com.readnocry.utils.EncodingDetector;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Log4j
public class BookStockServiceImpl implements BookStockService {

    @Value("${book-stock.location}")
    private String location;
    private final AppUserDao appUserDao;
    private final BookMetaDataDao bookMetaDataDao;

    public BookStockServiceImpl(AppUserDao appUserDao, BookMetaDataDao bookMetaDataDao) {
        this.appUserDao = appUserDao;
        this.bookMetaDataDao = bookMetaDataDao;
    }

    @Override
    @Transactional
    public void saveFileOnDisk(MultipartFile file, AppUser appUser, String bookTitle) throws IOException {
        log.info("Start saving file on disk: " + file.getOriginalFilename() + " for user: " + appUser);
        String filename = file.getOriginalFilename();
        Path rootLocation = Paths.get(location);
        if (filename == null || filename.isEmpty()) {
            log.error("Filename is invalid: " + filename);
            throw new IOException("Invalid file name");
        }
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }

        Path destinationFile = rootLocation.resolve(
                        Paths.get(appUser.getUsername() + "-" + filename))
                .normalize().toAbsolutePath();
        log.info("Final file path: " + destinationFile);

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile);
        }

        BookMetaData bookMetaData = new BookMetaData();
        bookMetaData.setFileName(file.getOriginalFilename());
        bookMetaData.setFilePath(destinationFile.toString());
        bookMetaData.setBookTitle(Objects.equals(bookTitle, "") ? file.getOriginalFilename() : bookTitle);
        bookMetaData.setPage(0);
        bookMetaData.setAppUser(appUser);
        bookMetaData.setPageSize(appUser.getAppUserSettings().getPageSize().getBytePageSize());
        bookMetaData.setCharset(EncodingDetector.detectEncoding(destinationFile));
        Set<BookMetaData> books = appUser.getBooks();
        books.add(bookMetaData);
        appUser.setBooks(books);
        appUserDao.save(appUser);
    }

    @Override
    @Transactional
    public void saveBaseBookOnDisk(File file, AppUser appUser, String bookTitle) throws IOException {
        log.info("Start saving base book on disk for user: " + appUser);
        String filename = file.getName();
        Path rootLocation = Paths.get(location);
        if (filename.isEmpty()) {
            log.error("Filename is invalid: " + filename);
            throw new IOException("Invalid file name");
        }
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }

        Path destinationFile = rootLocation.resolve(
                        Paths.get(appUser.getUsername() + "-" + filename))
                .normalize().toAbsolutePath();
        log.info("Final file path: " + destinationFile);

        Files.copy(file.toPath(), destinationFile);

        BookMetaData bookMetaData = new BookMetaData();
        bookMetaData.setFileName(filename);
        bookMetaData.setFilePath(destinationFile.toString());
        bookMetaData.setBookTitle(Objects.equals(bookTitle, "") ? filename : bookTitle);
        bookMetaData.setPage(0);
        bookMetaData.setAppUser(appUser);
        bookMetaData.setPageSize(PageSize.M.getBytePageSize());
        bookMetaData.setCharset(EncodingDetector.detectEncoding(destinationFile));
        Set<BookMetaData> books = appUser.getBooks();
        books.add(bookMetaData);
        appUser.setBooks(books);
        appUserDao.save(appUser);
    }

    @Override
    public Optional<BookMetaData> findById(Long bookMetaDataId) {
        log.info("Find book by ID: " + bookMetaDataId);
        return bookMetaDataDao.findById(bookMetaDataId);
    }

    @Override
    public BookMetaData save(BookMetaData bookMetaData) {
        log.info("Save book: " + bookMetaData);
        return bookMetaDataDao.save(bookMetaData);
    }

    @Override
    @Transactional
    public void delete(AppUser appUser, Long bookId) {
        log.info("Delete book: " + bookId);
        bookMetaDataDao.findById(bookId).ifPresentOrElse(bookMetaData -> {
            if (isUserOwnerOfBook(appUser, bookMetaData)) {
                appUser.getBooks().remove(bookMetaData);
                //bookMetaDataDao.deleteById(bookId);
                try {
                    Files.delete(Paths.get(bookMetaData.getFilePath()));
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }, () -> log.error("Book not found for ID: " + bookId));
    }

    @Override
    @Transactional
    public boolean isUserOwnerOfBook(AppUser user, BookMetaData bookMetaData) {
        var result = bookMetaData.getAppUser().equals(user);
        if (!result) {
            log.error("User: " + user + "is not an owner of the book: " + bookMetaData);
        }
        return result;
    }
}