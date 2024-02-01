package com.readnocry.dictionary.dao;

import com.readnocry.dictionary.entity.Dictionary;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DictionaryDao extends MongoRepository<Dictionary, String> {

    Dictionary findByAppUserId(Long userId);
}
