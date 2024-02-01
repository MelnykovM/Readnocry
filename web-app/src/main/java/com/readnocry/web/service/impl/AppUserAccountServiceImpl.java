package com.readnocry.web.service.impl;

import com.readnocry.CryptoTool;
import com.readnocry.dto.AppUserDTO;
import com.readnocry.dto.MailParamsDTO;
import com.readnocry.dto.enums.MailPurpose;
import com.readnocry.entity.AppUser;
import com.readnocry.rabbitmq.service.WebRabbitProducer;
import com.readnocry.service.AppUserService;
import com.readnocry.service.BookStockService;
import com.readnocry.service.TelegramAppUserService;
import com.readnocry.web.service.AppUserAccountService;
import lombok.extern.log4j.Log4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static com.readnocry.entity.enums.TelegramAppUserState.BASIC_STATE;

@Service
@Log4j
public class AppUserAccountServiceImpl implements AppUserAccountService {

    private final AppUserService appUserService;
    private final BookStockService bookStockService;
    private final TelegramAppUserService telegramAppUserService;
    private final CryptoTool cryptoTool;
    private final WebRabbitProducer webRabbitProducer;
    private final ResourceLoader resourceLoader;

    public AppUserAccountServiceImpl(AppUserService appUserService,
                                     BookStockService bookStockService,
                                     TelegramAppUserService telegramAppUserService,
                                     CryptoTool cryptoTool, WebRabbitProducer webRabbitProducer,
                                     ResourceLoader resourceLoader) {
        this.appUserService = appUserService;
        this.bookStockService = bookStockService;
        this.telegramAppUserService = telegramAppUserService;
        this.cryptoTool = cryptoTool;
        this.webRabbitProducer = webRabbitProducer;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void mailActivation(String cryptoUserId) {
        var userId = cryptoTool.idOf(cryptoUserId);
        log.info("Mail activation: " + userId);
        appUserService.findById(userId).ifPresentOrElse(appUser -> {
            appUser.setIsActive(true);
            appUserService.saveAppUser(appUser);
        }, () -> log.error("User not found while mail activation"));
    }

    @Override
    @Transactional
    public void telegramConnection(String cryptoUserId) {
        var userId = cryptoTool.idOf(cryptoUserId);
        log.info("Telegram connection: " + userId);
        appUserService.findById(userId).ifPresentOrElse(appUser -> {
            telegramAppUserService.findByEmail(appUser.getEmail()).ifPresentOrElse(telegramAppUser -> {
                appUser.setTelegramUserId(telegramAppUser.getTelegramUserId());
                appUser.setTelegramChatId(telegramAppUser.getTelegramChatId());
                appUser.setFirstName(telegramAppUser.getFirstName());
                appUser.setLastName(telegramAppUser.getLastName());
                appUserService.saveAppUser(appUser);
                telegramAppUser.setState(BASIC_STATE);
                telegramAppUser.setConnectedToAppUser(true);
                telegramAppUserService.saveTelegramAppUser(telegramAppUser);
            }, () -> log.error("Telegram user not found while mail activation"));
        }, () -> log.error("User not found while mail activation"));
    }

    @Override
    public void createAppUserAccount(AppUserDTO appUserDTO) {
        AppUser appUser = appUserService.createNewAppUser(appUserDTO);
        setupBaseBook(appUser);
        sendMail(appUser, MailPurpose.ACCOUNT_ACTIVATION);
    }

    @Override
    public void processSendMeMailsAgainCommand(AppUser appUser) {
        log.info("Start send me mail again " + appUser);
        var email = appUser.getEmail();
        if (!appUser.getIsActive()) {
            log.info("Sending activation mail for user: " + appUser);
            sendMail(appUser, MailPurpose.ACCOUNT_ACTIVATION);
        }
        var telegramAppUser = telegramAppUserService.findByEmail(email);
        if (telegramAppUser.isPresent() && !telegramAppUser.get().getConnectedToAppUser()) {
            log.info("Sending connecting mail for user: " + appUser);
            sendMail(appUser, MailPurpose.TELEGRAM_CONNECTION);
        }
    }

    private void sendMail(AppUser appUser, MailPurpose purpose) {
        var cryptoUserId = cryptoTool.hashOf(appUser.getId());
        var mailParams = MailParamsDTO.builder()
                .id(cryptoUserId)
                .emailTo(appUser.getEmail())
                .mailPurpose(purpose)
                .build();
        webRabbitProducer.produceSendMailRequest(mailParams);
    }

    @Transactional
    private void setupBaseBook(AppUser appUser) {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/Base-book.txt");
            File file = transferResourceToFile(resource);
            bookStockService.saveBaseBookOnDisk(file, appUser, "Harry Potter and the Philosopher's Stone");
        } catch (IOException e) {
            log.error("Error handling the file: " + e.getMessage(), e);
        }
    }

    private File transferResourceToFile(Resource resource) throws IOException {
        File tempFile = Files.createTempFile("Base-book", ".txt").toFile();
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }
}
