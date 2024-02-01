package com.readnocry.gpt.service;

import com.readnocry.dto.WebTranslationProcessingDTO;
import com.readnocry.gpt.dto.ChatGptRequestDTO;

public interface ChatGptWebTranslationMessageService {

    ChatGptRequestDTO createChatGptRequest(WebTranslationProcessingDTO request);

    ChatGptRequestDTO createFirstChatGptRequest(WebTranslationProcessingDTO request);
}
