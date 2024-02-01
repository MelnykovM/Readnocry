package com.readnocry.service;

import com.readnocry.dto.AppUserDTO;
import com.readnocry.entity.AppUser;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface AppUserService extends UserDetailsService {

    void saveAppUser(AppUser appUser);

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    AppUser createNewAppUser(AppUserDTO user);

    AppUser getCurrentUser();

    Optional<AppUser> findAppUserByTelegramUserId(Long id);

    Optional<AppUser> findById(Long id);

    AppUser getAuthorizedUser();

    AppUser addTokens(AppUser appUser, Integer tokens);
}
