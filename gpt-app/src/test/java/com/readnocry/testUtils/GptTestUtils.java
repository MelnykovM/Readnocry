package com.readnocry.testUtils;

import com.readnocry.dto.AppUserDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.BookMetaData;
import com.readnocry.service.AppUserService;
import com.readnocry.service.BookStockService;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.mockito.Mockito.when;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

public class GptTestUtils {

    private final AppUserService appUserService;
    private final BookStockService bookStockService;

    public GptTestUtils(AppUserService appUserService, BookStockService bookStockService) {
        this.appUserService = appUserService;
        this.bookStockService = bookStockService;
    }

    @Transactional(propagation = REQUIRES_NEW)
    public AppUser createAppUser(String username) {
        AppUserDTO appUserDTO = new AppUserDTO(username, "pass", username + "@gmail.com");
        AppUser appUser = appUserService.createNewAppUser(appUserDTO);
        return appUserService.findByUsername(appUser.getUsername()).orElseThrow();
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void setZeroTokensToAppUser(AppUser appUser) {
        appUser.setTokensCount(0);
        appUserService.saveAppUser(appUser);
    }

    @Transactional(propagation = REQUIRES_NEW)
    public BookMetaData createBookForAppUser(AppUser appUser) throws IOException {

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(Mockito.any())).thenReturn(false);
            mockedFiles.when(() -> Files.createDirectories(Mockito.any())).thenReturn(null);
            mockedFiles.when(() -> Files.copy(Mockito.any(InputStream.class), Mockito.any())).thenReturn(0L);
            mockedFiles.when(() -> Files.newInputStream(Mockito.any()))
                    .thenReturn(new ByteArrayInputStream("test content".getBytes(StandardCharsets.UTF_8)));

            MultipartFile mockFile = Mockito.mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn(appUser.getUsername() + "filename");

            bookStockService.saveFileOnDisk(mockFile, appUser, appUser.getUsername() + "-test");

            return appUserService.findByUsername(appUser.getUsername()).orElseThrow().getBooks().stream().findFirst().orElseThrow();
        }
    }
}
