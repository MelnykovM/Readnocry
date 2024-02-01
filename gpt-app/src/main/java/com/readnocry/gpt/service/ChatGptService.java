package com.readnocry.gpt.service;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.WebTranslationProcessingDTO;

public interface ChatGptService {

    void processInteraction(ChatGptInteractionDTO request);

    void processWebTranslationRequest(WebTranslationProcessingDTO request);
}
