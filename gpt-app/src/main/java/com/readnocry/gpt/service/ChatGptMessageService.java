package com.readnocry.gpt.service;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.gpt.dto.ChatGptRequestDTO;
import com.readnocry.gpt.dto.ChatGptResponseDTO;

public interface ChatGptMessageService {

    ChatGptRequestDTO createChatGptRequest(ChatGptInteractionDTO request, AppUser appUser);

    void addResponseToHistory(ChatGptInteractionDTO interaction, ChatGptResponseDTO chatGptResponseDTO, AppUser appUser);

    String processCommand(ChatGptInteractionDTO command, AppUser appUser);
}
