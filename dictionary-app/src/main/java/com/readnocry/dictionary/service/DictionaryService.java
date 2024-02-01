package com.readnocry.dictionary.service;

import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.WordDTO;

public interface DictionaryService {

    void addWordToUserDictionary(WordDTO translationDTO);

    void consumeGetDictionaryRequest(DictionaryDTO request);

    void deleteFromDictionary(WordDTO request);
}
