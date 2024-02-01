package com.readnocry.web.service;

import com.readnocry.dto.DictionaryDTO;
import com.readnocry.entity.AppUser;

public interface WebDictionaryService {

    void processGetDictionaryResponse(DictionaryDTO dictionary);

    void addWordToDictionary(AppUser appUser, String word, String transcription, String translation);

    void deleteFromDictionary(AppUser appUser, String wordId, String word, String transcription, String translation);

    void getAllDictionary(AppUser appUser);
}
