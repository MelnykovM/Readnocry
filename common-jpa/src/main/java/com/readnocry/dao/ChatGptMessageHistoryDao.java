package com.readnocry.dao;

import com.readnocry.entity.AppUser;
import com.readnocry.entity.ChatGptMessageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatGptMessageHistoryDao extends JpaRepository<ChatGptMessageHistory, Long> {

    Optional<ChatGptMessageHistory> findByAppUser(AppUser appUser);
}
