package com.readnocry.dao;

import com.readnocry.entity.TelegramAppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramAppUserDao extends JpaRepository<TelegramAppUser, Long> {

    Optional<TelegramAppUser> findByTelegramUserId(Long id);

    Optional<TelegramAppUser> findByTelegramChatId(Long id);

    Optional<TelegramAppUser> findByUsername(String username);

    Optional<TelegramAppUser> findById(Long id);

    Optional<TelegramAppUser> findByEmail(String email);
}
