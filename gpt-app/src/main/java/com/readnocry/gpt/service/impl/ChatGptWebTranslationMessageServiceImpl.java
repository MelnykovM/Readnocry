package com.readnocry.gpt.service.impl;

import com.readnocry.dto.WebTranslationProcessingDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.AppUserSettings;
import com.readnocry.exception.BookNotFoundException;
import com.readnocry.gpt.dto.ChatGptMessageDTO;
import com.readnocry.gpt.dto.ChatGptRequestDTO;
import com.readnocry.gpt.service.ChatGptWebTranslationMessageService;
import com.readnocry.service.BookStockService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Log4j
public class ChatGptWebTranslationMessageServiceImpl implements ChatGptWebTranslationMessageService {

    @Value("${gpt.model}")
    private String gptModel;

    // no words = 5 sec
    private final String promtV0 = "Translate the text into %s literary and send response in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format:\n{\n\"translation1\": \"text\"\n}";

    // 18 sec
    private final String promtV1 = "Translate the text into %s and translate separately the most interesting words or phraseological units of this text. Send response in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format (don't forget commas):\n{\n\"translation1\": \"text\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"original-text\",\n\"translation\": \"translation-text\",\n  \"transcription\": \"transcription-text\"\n }\n  ]\n}";

    private final String promtV2 = "Translate the text into %s and translate separately all phraseological units and figures of speech, then translate all words which are not included in phraseological units and figures of speech. Send response in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format (don't forget commas):\n{\n\"translation1\": \"text\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"original-text\",\n\"translation\": \"translation-text\",\n  \"transcription\": \"transcription-text\"\n }\n  ]\n}";

    private final String promtV3 = "Translate the text into %s and identify words, phraseological units, phrases, figures of speech from the text that most likely I don’t know - my level of English is upper-intermediate, and translate them separately. Send response in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format (don't forget commas):\n{\n\"translation1\": \"text\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"original-text\",\n\"translation\": \"translation-text\",\n  \"transcription\": \"transcription-text\"\n }\n  ]\n}";


    private final String promtV4 = promtV1;
    //private final String promtV1 = "I will send you a part of text in English and I am waiting from you in response good, quality literary translation taking into account all the phraseological units (idioms) and figures of speech of the entire part. \n I also want you to divide the text into words, phraseological units and established phrases and provide a separate translation and transcription of each. I need a translation into %s \n + I want the answer from you to be in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format:\n{\n\"translation1\": \"<text>\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"<text>\",\n\"translation\": \"<text>\",\n  \"transcription\": \"<text>\"\n }\n  ]\n}"; //21sec good translation
    //private final String promtV2 = "I will send you a part of text in English and I am waiting from you in response good, quality translation taking into account all the phraseological units (idioms) and figures of speech of the entire part. \n I also want you to divide the text into words, phraseological units and established phrases and provide a separate translation and transcription of each. I need a translation into %s (Do not translate easy for beginner level words or words combination) \n + I want the answer from you to be in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format:\n{\n\"translation1\": \"<text>\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"<text>\",\n\"translation\": \"<text>\",\n  \"transcription\": \"<text>\"\n }\n  ]\n}"; //46sec super detailed

    //private final String promtV1 = "I will send you a part of text in English and I am waiting from you in response good, quality literary translation taking into account all the phraseological units (idioms) and figures of speech of the entire part. \n I also want you to divide the text into words, phraseological units and established phrases and provide a separate translation and transcription of each. I need a translation into %s \n + I want the answer from you to be in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format:\n{\n\"translation1\": \"text\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"text\",\n\"translation\": \"text\",\n  \"transcription\": \"text\"\n }\n  ]\n}";
    //private final String promtV3 = "I will send you a part of text in English and I am waiting from you in response:\n 1. Literary translation taking into account all the phraseological units and figures of speech of the entire part.\n 2. Literal translation of all part.\n+\n I also want you to divide the text into words, phraseological units and established phrases and provide a separate translation and transcription of each. I need a translation into %s \n + I want the answer from you to be in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format:\n{\n\"translation1\": \"text\",\n  \"translation2\": \"text\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"text\",\n\"translation\": \"text\",\n  \"transcription\": \"text\"\n }\n  ]\n}";
    //private final String promtV3 = "Translate the text into %s and send response in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format:\n{\n\"translation1\": \"text\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"text\",\n\"translation\": \"text\",\n  \"transcription\": \"text\"\n }\n  ]\n}";


    //private final String promtV3 = "Translate the text into %s and and translate separately the most interesting words or phraseological units of this text. Send response in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format:\n{\n\"translation1\": \"text\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"text\",\n\"translation\": \"text\",\n  \"transcription\": \"text\"\n }\n  ]\n}"; //21sec looks faster than v1
    //private final String promtV3 = "Translate the text into %s and translate separately the most interesting words or phraseological units of this text. Send response in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format:\n{\n\"translation1\": \"text\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"text\",\n\"translation\": \"text\"\n }\n  ]\n}"; //12-13sec no transcription
    //private final String promtV3 = "Translate the text into %s and translate separately the most interesting words or phraseological units of this text. Send response in JSON format:\n{\n\"translation1\": \"text\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"text\",\n\"translation\": \"text\"\n }\n  ]\n}"; //16-18sec so strange
    //private final String promtV3 = "Provide text translation into %s in JSON format:\n{\n\"translation1\": \"text\",\n\"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n{\n \"words_combination_or_phraseological_unit_or_word\": \"text\",\n\"translation\": \"text\"\n }\n  ]\n}";//24sec
    //private final String promtV4 = "Translate the text into %s literary and send response in JSON format\n + send a JSON response without any additions (no words or letters before or after) only JSON in this format:\n{\n\"translation1\": \"text\"\n}"; //5sec


    private final BookStockService bookStockService;

    public ChatGptWebTranslationMessageServiceImpl(BookStockService bookStockService) {
        this.bookStockService = bookStockService;
    }

    @Override
    @Transactional
    public ChatGptRequestDTO createChatGptRequest(WebTranslationProcessingDTO request) {
        log.info("Creating ChatGpt request: " + request);
        return bookStockService.findById(request.getBookMetaDataId())
                .flatMap(bookMetaData -> Optional.ofNullable(bookMetaData.getAppUser()))
                .map(appUser -> {
                    List<ChatGptMessageDTO> messages = prepareChatGptWebTranslationMessagesDTO(request.getSentence(), appUser);
                    return ChatGptRequestDTO.builder()
                            .model(gptModel)
                            .messages(messages)
                            .build();
                })
                .orElseThrow(() -> {
                    log.error("Book not found while creating ChatGpt request: " + request);
                    return new BookNotFoundException(request.toString());
                });
    }

    @Override
    @Transactional
    public ChatGptRequestDTO createFirstChatGptRequest(WebTranslationProcessingDTO request) {
        log.info("Creating ChatGpt request: " + request);
        return bookStockService.findById(request.getBookMetaDataId())
                .flatMap(bookMetaData -> Optional.ofNullable(bookMetaData.getAppUser()))
                .map(appUser -> {
                    String promtToUse = getPromptString(appUser.getAppUserSettings(), promtV0);
                    List<ChatGptMessageDTO> messages = List.of(new ChatGptMessageDTO("user", promtToUse), new ChatGptMessageDTO("user", request.getSentence()));
                    return ChatGptRequestDTO.builder()
                            .model(gptModel)
                            .messages(messages)
                            .build();
                })
                .orElseThrow(() -> {
                    log.error("Book not found while creating ChatGpt request: " + request);
                    return new BookNotFoundException(request.toString());
                });
    }

    private List<ChatGptMessageDTO> prepareChatGptWebTranslationMessagesDTO(String requestText, AppUser appUser) {
        log.info("Preparing ChatGpt messages for user: " + appUser.getId());
        AppUserSettings settings = appUser.getAppUserSettings();
        String basePromtToUse = switch (settings.getPromptVersion()) {
            case V1 -> promtV1;
            case V2 -> promtV2;
            case V3 -> promtV3;
            case V4 -> promtV4;
        };
        String promtToUse = getPromptString(settings, basePromtToUse);
        return List.of(new ChatGptMessageDTO("user", promtToUse), new ChatGptMessageDTO("user", requestText));
    }

    private static String getPromptString(AppUserSettings settings, String basePromtToUse) {
        String promtToUse = "";
        promtToUse = String.format(basePromtToUse, settings.getTranslateTo().getDisplayName());

        /*if (settings.getPromptVersion().equals(V1)){
            promtToUse = String.format(basePromtToUse, settings.getTranslateTo().getDisplayName());
        } else if (settings.getPromptVersion().equals(V2)) {
            promtToUse = String.format(basePromtToUse, settings.getTranslateTo().getDisplayName());
        } else {
            promtToUse = String.format(basePromtToUse, settings.getLanguageProficiencyLevel().getDisplayLevelName(), settings.getTranslateTo().getDisplayName());
        }*/
        return promtToUse;
    }
}
