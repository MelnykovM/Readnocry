package com.readnocry.web.service;

import com.readnocry.dto.AppUserDTO;
import com.readnocry.entity.AppUser;

public interface AppUserAccountService {

    void mailActivation(String cryptoUserId);

    void telegramConnection(String cryptoUserId);

    void createAppUserAccount(AppUserDTO appUserDTO);

    void processSendMeMailsAgainCommand(AppUser appUser);
}
