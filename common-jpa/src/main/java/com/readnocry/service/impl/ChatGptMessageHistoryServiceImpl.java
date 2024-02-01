package com.readnocry.service.impl;

import com.readnocry.dao.ChatGptMessageHistoryDao;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.ChatGptMessageHistory;
import com.readnocry.service.ChatGptMessageHistoryService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j
public class ChatGptMessageHistoryServiceImpl implements ChatGptMessageHistoryService {

    private final ChatGptMessageHistoryDao chatGptMessageHistoryDao;

    public ChatGptMessageHistoryServiceImpl(ChatGptMessageHistoryDao chatGptMessageHistoryDao) {
        this.chatGptMessageHistoryDao = chatGptMessageHistoryDao;
    }

    @Override
    public Optional<ChatGptMessageHistory> findByAppUser(AppUser appUser) {
        log.info("Find by user: " + appUser);
        return chatGptMessageHistoryDao.findByAppUser(appUser);
    }

    @Override
    public void delete(ChatGptMessageHistory chatGptMessageHistory) {
        log.info("Delete " + chatGptMessageHistory);
        chatGptMessageHistoryDao.delete(chatGptMessageHistory);
    }

    @Override
    public ChatGptMessageHistory save(ChatGptMessageHistory chatGptMessageHistory) {
        log.info("Save " + chatGptMessageHistory);
        return chatGptMessageHistoryDao.save(chatGptMessageHistory);
    }
}
