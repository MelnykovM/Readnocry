package com.readnocry.gpt.service.impl;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.enums.ChatGptCommand;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.ChatGptMessage;
import com.readnocry.entity.ChatGptMessageHistory;
import com.readnocry.gpt.dto.ChatGptMessageDTO;
import com.readnocry.gpt.dto.ChatGptRequestDTO;
import com.readnocry.gpt.dto.ChatGptResponseDTO;
import com.readnocry.gpt.mapper.ChatGptMessageMapper;
import com.readnocry.gpt.service.ChatGptMessageService;
import com.readnocry.service.ChatGptMessageHistoryService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.readnocry.entity.enums.ChatMessageRole.ASSISTANT;
import static com.readnocry.entity.enums.ChatMessageRole.USER;

@Service
@Log4j
public class ChatGptMessageServiceImpl implements ChatGptMessageService {

    @Value("${gpt.model}")
    private String gptModel;
    private final ChatGptMessageHistoryService chatGptMessageHistoryService;
    private final ChatGptMessageMapper mapper;

    public ChatGptMessageServiceImpl(ChatGptMessageHistoryService chatGptMessageHistoryService,
                                     ChatGptMessageMapper mapper) {
        this.chatGptMessageHistoryService = chatGptMessageHistoryService;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public String processCommand(ChatGptInteractionDTO command, AppUser appUser) {
        AtomicReference<String> result = new AtomicReference<>("");
        chatGptMessageHistoryService.findByAppUser(appUser).ifPresentOrElse(usersChatMessageHistory -> {
            if (command.getCommand().equals(ChatGptCommand.CLEAN_CHAT_GPT_HISTORY)) {
                chatGptMessageHistoryService.delete(usersChatMessageHistory);
                log.info("Chat history cleaned for user: " + appUser);
                result.set("History is cleaned.");
            }
            if (command.getCommand().equals(ChatGptCommand.SHOW_CHAT_GPT_HISTORY)) {
                log.info("Showing chat history for user: " + appUser);
                result.set(mapper.mapChatGptMessageHistoryToString(usersChatMessageHistory));
            }
        }, () -> {
            log.info("No history found for user: " + appUser);
            result.set("No history.");
        });
        return result.get();
    }

    @Override
    @Transactional
    public ChatGptRequestDTO createChatGptRequest(ChatGptInteractionDTO request, AppUser appUser) {
        String requestText = request.getMessage();
        List<ChatGptMessageDTO> messages = prepareChatGptMessagesDTO(appUser, requestText);
        return ChatGptRequestDTO.builder().model(gptModel).messages(messages).build();
    }

    private List<ChatGptMessageDTO> prepareChatGptMessagesDTO(AppUser appUser, String requestText) {
        if (!appUser.getSaveChatGptHistory()) {
            log.info("Preparing just one chatGpt message for user: " + appUser);
            return List.of(new ChatGptMessageDTO("user", requestText));
        } else {
            Optional<ChatGptMessageHistory> usersChatMessageHistory = chatGptMessageHistoryService.findByAppUser(appUser);
            ChatGptMessageHistory chatMessageHistory = usersChatMessageHistory
                    .map(chatGptMessageHistory -> addMessageToHistory(chatGptMessageHistory, requestText))
                    .orElseGet(() -> createNewMessageHistory(appUser, requestText));
            return mapper.mapChatGptMessageHistoryToDTO(chatMessageHistory);
        }
    }

    @Override
    @Transactional
    public void addResponseToHistory(ChatGptInteractionDTO interaction,
                                     ChatGptResponseDTO chatGptResponseDTO,
                                     AppUser appUser) {
        if (appUser.getSaveChatGptHistory()) {
            chatGptMessageHistoryService.findByAppUser(appUser).ifPresentOrElse(chatGptMessageHistory -> {
                log.info("Adding response to history for user: " + appUser);
                var message = chatGptResponseDTO.getChoices().get(0).getMessage().getContent();
                var tokens = chatGptResponseDTO.getUsage().getTotal_tokens();
                ChatGptMessage newMessage = ChatGptMessage.builder()
                        .role(ASSISTANT)
                        .content(message)
                        .build();
                List<ChatGptMessage> messageHistory = chatGptMessageHistory.getChatGptMessages();
                messageHistory.add(newMessage);
                ChatGptMessageHistory persistChatGptMessageHistory = chatGptMessageHistory.toBuilder()
                        .chatGptMessages(messageHistory)
                        .tokenSum(tokens)
                        .build();
                chatGptMessageHistoryService.save(persistChatGptMessageHistory);
            }, () -> log.error("No history for user: " + appUser));
        }
    }

    private ChatGptMessageHistory addMessageToHistory(ChatGptMessageHistory chatGptMessageHistory, String requestText) {
        log.info("Adding new message to history: " + chatGptMessageHistory);
        ChatGptMessage newMessage = ChatGptMessage.builder().role(USER).content(requestText).build();
        List<ChatGptMessage> messageHistory = chatGptMessageHistory.getChatGptMessages();
        messageHistory.add(newMessage);
        ChatGptMessageHistory persistChatGptMessageHistory = chatGptMessageHistory.toBuilder()
                .chatGptMessages(messageHistory)
                .build();
        return chatGptMessageHistoryService.save(persistChatGptMessageHistory);
    }

    private ChatGptMessageHistory createNewMessageHistory(AppUser appUser, String requestText) {
        log.info("Creating new message history for user: " + appUser);
        ChatGptMessage firstMessage = ChatGptMessage.builder().role(USER).content(requestText).build();
        ChatGptMessageHistory persistChatGptMessageHistory = ChatGptMessageHistory.builder()
                .appUser(appUser)
                .chatGptMessages(List.of(firstMessage))
                .tokenSum(0)
                .build();
        return chatGptMessageHistoryService.save(persistChatGptMessageHistory);
    }
}
