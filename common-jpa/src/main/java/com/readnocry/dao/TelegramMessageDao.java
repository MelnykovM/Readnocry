package com.readnocry.dao;

import com.readnocry.entity.TelegramMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramMessageDao extends JpaRepository<TelegramMessage, Long> {
}
