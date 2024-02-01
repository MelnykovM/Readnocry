package com.readnocry.telegramIntegration.service;

import com.readnocry.dto.ChatGptInteractionDTO;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ChatGptService {

    void chatGptRequest(ChatGptInteractionDTO request);

    void processChatGptResponse(ChatGptInteractionDTO response);

    void chatGptProcessRequest(Update update, String cmd);

    void sendAnswerWithChatGptKeyboardMarkup(Long chatId, String message);
}
