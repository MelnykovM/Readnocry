package com.readnocry.dictionary.dao;

import com.readnocry.dictionary.entity.Word;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WordDao extends MongoRepository<Word, String> {
}
