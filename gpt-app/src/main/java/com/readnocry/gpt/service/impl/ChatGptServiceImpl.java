package com.readnocry.gpt.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.TranslationResultDTO;
import com.readnocry.dto.WebTranslationProcessingDTO;
import com.readnocry.dto.WordDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.gpt.dto.ChatGptRequestDTO;
import com.readnocry.gpt.dto.ChatGptResponseDTO;
import com.readnocry.gpt.dto.SentenceTranslationDTO;
import com.readnocry.gpt.service.ChatGptMessageService;
import com.readnocry.gpt.service.ChatGptService;
import com.readnocry.gpt.service.ChatGptWebTranslationMessageService;
import com.readnocry.rabbitmq.service.ChatRabbitProducer;
import com.readnocry.service.AppUserService;
import com.readnocry.service.BookStockService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.readnocry.RabbitQueue.CHAT_GPT_RESPONSE;
import static com.readnocry.RabbitQueue.TRANSLATION_RESPONSE;

@Service
@Log4j
public class ChatGptServiceImpl implements ChatGptService {

    @Value("${gpt.maxRetriesCount}")
    private int maxRetriesCount;
    @Value("${gpt.url}")
    private String gptUrl;
    @Qualifier("openaiRestTemplate")
    @Autowired
    private RestTemplate restTemplate;
    private final ChatRabbitProducer chatRabbitProducer;
    private final ChatGptMessageService chatGptMessageService;
    private final BookStockService bookStockService;
    private final AppUserService appUserService;
    private final ChatGptWebTranslationMessageService chatGptWebTranslationMessageService;

    public ChatGptServiceImpl(ChatRabbitProducer chatRabbitProducer,
                              ChatGptMessageService chatGptMessageService,
                              BookStockService bookStockService,
                              AppUserService appUserService,
                              ChatGptWebTranslationMessageService chatGptWebTranslationMessageService) {
        this.chatRabbitProducer = chatRabbitProducer;
        this.chatGptMessageService = chatGptMessageService;
        this.bookStockService = bookStockService;
        this.appUserService = appUserService;
        this.chatGptWebTranslationMessageService = chatGptWebTranslationMessageService;
    }

    @Override
    public void processInteraction(ChatGptInteractionDTO request) {
        log.info("Processing interaction: " + request);
        appUserService.findById(request.getAppUserId())
                .ifPresentOrElse(appUser -> {
                    if (request.getIsCommand()) {
                        processCommand(request, appUser);
                    } else {
                        processTextRequest(request, appUser);
                    }
                }, () -> {
                    log.error("AppUser not found: " + request);
                    sendInteractionErrorResponse(request, "User not found.");
                });
    }

    private void processTextRequest(ChatGptInteractionDTO interaction, AppUser appUser) {
        if (appUser.getTokensCount() == 0) {
            sendInteractionErrorResponse(interaction, "You are out of tokens balance. =(");
        } else {
            try {
                ChatGptRequestDTO request = chatGptMessageService.createChatGptRequest(interaction, appUser);
                CompletableFuture<ChatGptResponseDTO> futureResponse = CompletableFuture.supplyAsync(() -> restTemplate.postForObject(gptUrl, request, ChatGptResponseDTO.class));
                ChatGptResponseDTO response = futureResponse.join();
                chatGptMessageService.addResponseToHistory(interaction, response, appUser);
                String result = response.getChoices().get(0).getMessage().getContent();
                interaction.setMessage(result);
                processTokens(appUser, response);
                chatRabbitProducer.produceTelegramResponse(CHAT_GPT_RESPONSE, interaction);
            } catch (Exception e) {
                log.error("Error processing text request: " + interaction, e);
                sendInteractionErrorResponse(interaction, "Oooops... Something went wrong.");
            }
        }
    }

    private void processCommand(ChatGptInteractionDTO command, AppUser appUser) {
        log.info("Processing command: " + command);
        try {
            var result = chatGptMessageService.processCommand(command, appUser);
            command.setMessage(result);
            chatRabbitProducer.produceTelegramResponse(CHAT_GPT_RESPONSE, command);
        } catch (Exception e) {
            log.error("Error processing command: " + command, e);
            sendInteractionErrorResponse(command, "Error processing command.");
        }
    }

    @Override
    public void processWebTranslationRequest(WebTranslationProcessingDTO translationProcessing) {
        log.info("Processing command: " + translationProcessing);
        bookStockService.findById(translationProcessing.getBookMetaDataId())
                .flatMap(bookMetaData -> Optional.ofNullable(bookMetaData.getAppUser()))
                .ifPresentOrElse(appUser -> translateRequest(translationProcessing, appUser), () -> {
                    log.error("BookMetaData or associated AppUser not found: " + translationProcessing);
                    sendTranslationErrorResponse(translationProcessing, null);
                });
    }

    private void translateRequest(WebTranslationProcessingDTO translationProcessing, AppUser appUser) {
        if (appUser.getTokensCount() == 0) {
            log.info("Out of tokens balance, user: " + appUser.getId());
            sendTranslationErrorResponse(translationProcessing, "You are out of tokens balance. =(");
        } else {
            int maxRetries = maxRetriesCount;
            int retryCount = 0;
            while (retryCount < maxRetries) {
                try {
                    log.info("ChatGptRequest start");
                    ChatGptRequestDTO firstTranslationRequest = chatGptWebTranslationMessageService.createFirstChatGptRequest(translationProcessing);
                    ChatGptRequestDTO translationRequest = chatGptWebTranslationMessageService.createChatGptRequest(translationProcessing);

                    CompletableFuture<ChatGptResponseDTO> firstFutureResponse = CompletableFuture.supplyAsync(() -> restTemplate.postForObject(gptUrl, firstTranslationRequest, ChatGptResponseDTO.class));
                    CompletableFuture<ChatGptResponseDTO> futureResponse = CompletableFuture.supplyAsync(() -> restTemplate.postForObject(gptUrl, translationRequest, ChatGptResponseDTO.class));

                    ChatGptResponseDTO firstResponse = firstFutureResponse.join();
                    String result = firstResponse.getChoices().get(0).getMessage().getContent();
                    log.info(result);
                    ObjectMapper mapper = new ObjectMapper();
                    SentenceTranslationDTO obj = mapper.readValue(result, SentenceTranslationDTO.class);
                    TranslationResultDTO translationResult = new TranslationResultDTO(obj.getTranslation1(), obj.getTranslation2(), new ArrayList<>());
                    translationProcessing.setTranslationResultDTO(translationResult);
                    chatRabbitProducer.produceWebTranslationResponse(TRANSLATION_RESPONSE, translationProcessing);

                    processTokens(appUser, firstResponse);

                    ChatGptResponseDTO response = futureResponse.join();
                    result = response.getChoices().get(0).getMessage().getContent();
                    log.info(result);

                    obj = mapper.readValue(result, SentenceTranslationDTO.class);
                    List<WordDTO> updatedWords = new ArrayList<>();
                    if (obj.getWords() != null) {
                        updatedWords = obj.getWords().stream().map(word -> new WordDTO(appUser.getId(), null, word.getWord(), processTranscription(word.getTranscription()), word.getTranslation())).toList();
                    }
                    translationResult = new TranslationResultDTO(obj.getTranslation1(), obj.getTranslation2(), updatedWords);
                    translationProcessing.setTranslationResultDTO(translationResult);

                    chatRabbitProducer.produceWebTranslationResponse(TRANSLATION_RESPONSE, translationProcessing);
                    processTokens(appUser, response);
                    break;
                } catch (JsonProcessingException e) {
                    log.error("Error during parsing chatGPT response. " + e.getMessage());
                    retryCount++;
                } catch (Exception e) {
                    log.error("Unexpected error: " + e.getMessage());
                    sendTranslationErrorResponse(translationProcessing, null);
                    break;
                }
            }
            if (retryCount == maxRetries) {
                log.error("Max retries reached.");
                sendTranslationErrorResponse(translationProcessing, null);
            }
        }
    }

    private String processTranscription(String transcription) {
        if (transcription == null) {
            return "[]";
        }
        String processedTranscription = transcription;
        if (!processedTranscription.startsWith("[")) {
            processedTranscription = "[" + processedTranscription;
        }
        if (!processedTranscription.endsWith("]")) {
            processedTranscription = processedTranscription + "]";
        }
        return processedTranscription;
    }

    private void processTokens(AppUser appUser, ChatGptResponseDTO response) {
        log.info("Processing tokens for user: " + appUser);
        var tokensBefore = appUser.getTokensCount();
        var tokenAfter = tokensBefore - response.getUsage().getTotal_tokens();
        appUser.setTokensCount(Math.max(tokenAfter, 0));
        appUserService.saveAppUser(appUser);
    }

    public void sendTranslationErrorResponse(WebTranslationProcessingDTO translationProcessing, String message) {
        translationProcessing.setTranslationResultDTO(TranslationResultDTO.builder()
                .translation1(message != null ? message : "Oooops... Something went wrong with the translation.")
                .translation2("")
                .words(List.of())
                .build());
        chatRabbitProducer.produceWebTranslationResponse(TRANSLATION_RESPONSE, translationProcessing);
    }

    public void sendInteractionErrorResponse(ChatGptInteractionDTO interaction, String message) {
        interaction.setMessage(message);
        chatRabbitProducer.produceTelegramResponse(CHAT_GPT_RESPONSE, interaction);
    }
}
