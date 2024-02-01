package com.readnocry.web.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readnocry.dto.TranslationResultDTO;
import com.readnocry.dto.WebTranslationProcessingDTO;
import com.readnocry.rabbitmq.service.WebRabbitProducer;
import com.readnocry.web.controller.WebSocketController;
import com.readnocry.web.service.WebTranslationService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

@Service
@Log4j
public class WebTranslationServiceImpl implements WebTranslationService {

    private final WebRabbitProducer webRabbitProducer;
    private final WebSocketController webSocketController;

    public WebTranslationServiceImpl(WebRabbitProducer webRabbitProducer,
                                     WebSocketController webSocketController) {
        this.webRabbitProducer = webRabbitProducer;
        this.webSocketController = webSocketController;
    }

    @Override
    public void sendRequestForTranslation(WebTranslationProcessingDTO request) {
        log.info("Send request for translation: " + request);
        webSocketController.showLoadingAnimation(request.getUsername());
        webRabbitProducer.produceTranslationRequest(request);
    }

    @Override
    public void processTranslationResponse(WebTranslationProcessingDTO response) {
        log.info("Process translation response: " + response);
        try {
            if (response.getTranslationResultDTO().getTranslation2() == null) {
                TranslationResultDTO translationResultDTO = response.getTranslationResultDTO();
                translationResultDTO.setTranslation2("");
                response.setTranslationResultDTO(translationResultDTO);
            }
            String jsonMessage = new ObjectMapper().writeValueAsString(response);
            webSocketController.sendTranslationContent(response.getUsername(), jsonMessage);
        } catch (JsonProcessingException e) {
            log.info("Error mapping translation response: " + response, e);
        }
    }
}
