package com.readnocry.service.impl;

import com.readnocry.dao.AppUserDao;
import com.readnocry.dao.BookMetaDataDao;
import com.readnocry.entity.AppUserSettings;
import com.readnocry.entity.BookMetaData;
import com.readnocry.service.AppUserService;
import com.readnocry.service.SettingsService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Log4j
public class SettingsServiceImpl implements SettingsService {

    private final AppUserDao appUserDao;
    private final AppUserService appUserService;
    private final BookMetaDataDao bookMetaDataDao;

    public SettingsServiceImpl(AppUserDao appUserDao, AppUserService appUserService, BookMetaDataDao bookMetaDataDao) {
        this.appUserDao = appUserDao;
        this.appUserService = appUserService;
        this.bookMetaDataDao = bookMetaDataDao;
    }

    @Override
    @Transactional
    public void saveSettings(AppUserSettings settings) {
        log.info("Start saving settings: " + settings);
        appUserDao.findById(settings.getId()).ifPresentOrElse(appUser -> {
            appUser.setAppUserSettings(settings);
            appUserService.saveAppUser(appUser);
            Set<BookMetaData> bookMetaDataSet = appUser.getBooks();
            bookMetaDataSet.forEach(book -> {
                var oldPageSize = book.getPageSize();
                var newPageSize = settings.getPageSize().getBytePageSize();
                if (oldPageSize != newPageSize) {
                    book.setPageSize(newPageSize);
                    book.setPage(0);
                    bookMetaDataDao.save(book);
                }
            });
        }, () -> log.error("User not found for ID: " + settings.getId()));
    }
}
