package com.readnocry.dao;

import com.readnocry.entity.ChatGptMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatGptMessageDao extends JpaRepository<ChatGptMessage, Long> {
}
