package com.readnocry.dao;

import com.readnocry.entity.BookMetaData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookMetaDataDao extends JpaRepository<BookMetaData, Long> {
}
