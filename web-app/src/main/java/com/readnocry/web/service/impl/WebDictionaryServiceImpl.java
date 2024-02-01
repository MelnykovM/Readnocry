package com.readnocry.web.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.WordDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.rabbitmq.service.WebRabbitProducer;
import com.readnocry.web.controller.WebSocketController;
import com.readnocry.web.service.WebDictionaryService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

@Service
@Log4j
public class WebDictionaryServiceImpl implements WebDictionaryService {

    private final WebSocketController webSocketController;
    private final WebRabbitProducer webRabbitProducer;

    public WebDictionaryServiceImpl(WebSocketController webSocketController,
                                    WebRabbitProducer webRabbitProducer) {
        this.webSocketController = webSocketController;
        this.webRabbitProducer = webRabbitProducer;
    }

    @Override
    public void processGetDictionaryResponse(DictionaryDTO dictionary) {
        log.info("Processing dictionary response: " + dictionary);
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(dictionary);
            webSocketController.sendDictionaryContent(dictionary.getUsername(), jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("Error processing dictionary response: " + dictionary, e);
            webSocketController.sendErrorContent(dictionary.getUsername(), "{\"message\": \"Oooops... Something went wrong with loading the dictionary.\"}");
        }
    }

    @Override
    public void addWordToDictionary(AppUser appUser,
                                    String word,
                                    String transcription,
                                    String translation) {
        WordDTO wordDTO = new WordDTO(appUser.getId(), null, word, transcription, translation);
        log.info("Add word to dictionary: " + wordDTO);
        webRabbitProducer.produceTranslationToDictionaryRequest(wordDTO);
        if (appUser.getTelegramChatId() != null) {
            webRabbitProducer.produceTranslationToTelegramRequest(wordDTO);
        }
    }

    @Override
    public void deleteFromDictionary(AppUser appUser,
                                     String wordId,
                                     String word,
                                     String transcription,
                                     String translation) {
        WordDTO wordDTO = new WordDTO(appUser.getId(), wordId, word, transcription, translation);
        log.info("Delete word from dictionary: " + wordDTO);
        webRabbitProducer.produceDeleteFromDictionaryRequest(wordDTO);
    }

    @Override
    public void getAllDictionary(AppUser appUser) {
        log.info("Get dictionary for user: " + appUser);
        DictionaryDTO request = new DictionaryDTO(appUser.getUsername(), appUser.getId(), null, null, null);
        webRabbitProducer.produceGetDictionaryRequest(request);
    }
}
