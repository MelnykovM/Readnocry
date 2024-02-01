package com.readnocry.service.impl;

import com.readnocry.dao.AppUserDao;
import com.readnocry.dao.AppUserSettingsDao;
import com.readnocry.dao.TelegramAppUserDao;
import com.readnocry.dto.AppUserDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.AppUserSettings;
import com.readnocry.entity.enums.Language;
import com.readnocry.entity.enums.LanguageProficiencyLevel;
import com.readnocry.entity.enums.PageSize;
import com.readnocry.entity.enums.PromptVersion;
import com.readnocry.exception.AuthorizedUserNotFoundException;
import com.readnocry.exception.EmailExistsException;
import com.readnocry.exception.UsernameExistsException;
import com.readnocry.service.AppUserService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

@Log4j
@Service
@EnableWebSecurity
public class AppUserServiceImpl implements AppUserService {

    @Autowired
    private AppUserDao appUserDao;
    @Autowired
    private AppUserSettingsDao appUserSettingsDao;
    @Autowired
    private TelegramAppUserDao telegramAppUserDao;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Optional<AppUser> findByUsername(String username) {
        return appUserDao.findByUsername(username);
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return appUserDao.findByEmail(email);
    }

    @Override
    @Transactional
    public AppUser createNewAppUser(AppUserDTO user) {
        log.info("Start creating new user: " + user);
        if (appUserDao.findByUsername(user.getUsername()).isPresent()) {
            log.info("User with this username already exists: " + user);
            throw new UsernameExistsException("User with this username already exists.");
        }
        if (appUserDao.findByEmail(user.getEmail()).isPresent()) {
            log.info("User with this email already exists: " + user);
            throw new EmailExistsException("User with this email already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        AppUser appUser = appUserDao.save(AppUser.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .tokensCount(11111)
                .saveChatGptHistory(true)
                .isActive(false)
                .books(new HashSet<>())
                .build());
        appUserSettingsDao.save(AppUserSettings.builder()
                .languageProficiencyLevel(LanguageProficiencyLevel.LOW)
                .translateTo(Language.UKRAINIAN)
                .promptVersion(PromptVersion.V1)
                .pageSize(PageSize.M)
                .appUser(appUser)
                .build());
        log.info("User saved: " + appUser);
        return appUser;
    }

    @Override
    public AppUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return appUserDao.getById(((AppUser) principal).getId());
        }
        return null;
    }

    @Override
    public AppUser getAuthorizedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return appUserDao.getById(((AppUser) principal).getId());
        }
        throw new AuthorizedUserNotFoundException();
    }

    @Override
    @Transactional
    public AppUser addTokens(AppUser appUser, Integer tokens) {
        log.info("Add tokens - " + tokens + " to user: " + appUser);
        appUser.setTokensCount(appUser.getTokensCount() + tokens);
        return appUserDao.save(appUser);
    }

    @Override
    public Optional<AppUser> findAppUserByTelegramUserId(Long id) {
        log.info("Find user by telegram user ID: " + id);
        return appUserDao.findByTelegramUserId(id);
    }

    @Override
    public Optional<AppUser> findById(Long id) {
        log.info("Find user by ID: " + id);
        return appUserDao.findById(id);
    }

    @Override
    public void saveAppUser(AppUser appUser) {
        log.info("Save user: " + appUser);
        appUserDao.save(appUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
