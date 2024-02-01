package com.readnocry.web.service;

import com.readnocry.dto.WebTranslationProcessingDTO;

public interface WebTranslationService {

    void sendRequestForTranslation(WebTranslationProcessingDTO request);

    void processTranslationResponse(WebTranslationProcessingDTO response);
}
