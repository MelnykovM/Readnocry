package com.readnocry.service;

import com.readnocry.entity.AppUser;
import com.readnocry.entity.ChatGptMessageHistory;

import java.util.Optional;

public interface ChatGptMessageHistoryService {

    Optional<ChatGptMessageHistory> findByAppUser(AppUser appUser);

    void delete(ChatGptMessageHistory chatGptMessageHistory);

    ChatGptMessageHistory save(ChatGptMessageHistory chatGptMessageHistory);
}
