package com.readnocry.gpt.mapper;

import com.readnocry.entity.ChatGptMessage;
import com.readnocry.entity.ChatGptMessageHistory;
import com.readnocry.gpt.dto.ChatGptMessageDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatGptMessageMapper {

    public List<ChatGptMessageDTO> mapChatGptMessageToDTO(List<ChatGptMessage> chatGptMessages) {
        return chatGptMessages.stream().map(this::mapChatGptMessageToDTO).collect(Collectors.toList());
    }

    public ChatGptMessageDTO mapChatGptMessageToDTO(ChatGptMessage chatGptMessage) {
        return ChatGptMessageDTO.builder()
                .role(chatGptMessage.getRole().toString())
                .content(chatGptMessage.getContent())
                .build();
    }

    public List<ChatGptMessageDTO> mapChatGptMessageHistoryToDTO(ChatGptMessageHistory chatGptMessageHistory) {
        return chatGptMessageHistory.getChatGptMessages().stream()
                .map(this::mapChatGptMessageToDTO).collect(Collectors.toList());
    }

    public String mapChatGptMessageHistoryToString(ChatGptMessageHistory chatGptMessageHistory) {
        var messageText = chatGptMessageHistory.getChatGptMessages().stream()
                .map(ChatGptMessage::getContent).toList().toString();
        return "token sum = " + chatGptMessageHistory.getTokenSum() + "     " + messageText;
    }
}
