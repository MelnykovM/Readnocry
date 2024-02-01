package com.readnocry.service;

import com.readnocry.entity.AppUser;
import com.readnocry.entity.BookMetaData;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public interface BookStockService {

    void saveFileOnDisk(MultipartFile file, AppUser appUser, String bookTitle) throws IOException;

    void saveBaseBookOnDisk(File file, AppUser appUser, String bookTitle) throws IOException;

    Optional<BookMetaData> findById(Long bookMetaDataId);

    BookMetaData save(BookMetaData bookMetaData);

    void delete(AppUser appUser, Long bookId);

    boolean isUserOwnerOfBook(AppUser user, BookMetaData bookMetaData);
}
