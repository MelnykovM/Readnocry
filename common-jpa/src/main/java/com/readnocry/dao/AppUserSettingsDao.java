package com.readnocry.dao;

import com.readnocry.entity.AppUserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserSettingsDao extends JpaRepository<AppUserSettings, Long> {
}
