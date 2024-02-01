package com.readnocry.integrationTest;

import com.readnocry.dto.WebTranslationProcessingDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.BookMetaData;
import com.readnocry.gpt.dto.*;
import com.readnocry.service.AppUserService;
import com.readnocry.service.BookStockService;
import com.readnocry.testConfiguration.PostgreSQLTestConfig;
import com.readnocry.testConfiguration.RabbitMqTestConfig;
import com.readnocry.testUtils.GptTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;

import static com.readnocry.RabbitQueue.TRANSLATION_REQUEST;
import static com.readnocry.RabbitQueue.TRANSLATION_RESPONSE;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {RabbitMqTestConfig.class, PostgreSQLTestConfig.class})
public class GptTranslationIntegrationTest {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private BookStockService bookStockService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private RestTemplate openaiRestTemplate;

    @Value("${gpt.url}")
    private String gptUrl;

    @BeforeEach
    public void setUp() {
        UsageDTO usageDTO = new UsageDTO(42);
        ChatGptMessageDTO chatGptMessageDTO = new ChatGptMessageDTO("USER", "{\n  \"translation1\": \"test1.\",\n  \"translation2\": \"test2.\",\n  \"words_combinations_and_phraseological_units_and_words_of_this_sentence\": [\n    {\n      \"words_combination_or_phraseological_unit_or_word\": \"word1\",\n      \"translation\": \"translation1\",\n      \"transcription\": \"transcription1\"\n    }\n  ]\n}");
        ChatGptChoiceDTO chatGptChoiceDTO = new ChatGptChoiceDTO(0, chatGptMessageDTO);
        when(openaiRestTemplate.postForObject(eq(gptUrl), any(ChatGptRequestDTO.class), eq(ChatGptResponseDTO.class))).thenReturn(new ChatGptResponseDTO(List.of(chatGptChoiceDTO), usageDTO));

    }

    @Test
    public void happyPathTest() throws InterruptedException, IOException {
        GptTestUtils gptTestUtils = new GptTestUtils(appUserService, bookStockService);
        AppUser appUser = gptTestUtils.createAppUser("username");
        BookMetaData bookMetaData = gptTestUtils.createBookForAppUser(appUser);

        WebTranslationProcessingDTO webTranslationProcessingDTO = new WebTranslationProcessingDTO(appUser.getUsername(), appUser.getId(), "test", bookMetaData.getId(), null);

        rabbitTemplate.convertAndSend(TRANSLATION_REQUEST, webTranslationProcessingDTO);

        WebTranslationProcessingDTO webTranslationProcessingResponse = (WebTranslationProcessingDTO) rabbitTemplate.receiveAndConvert(TRANSLATION_RESPONSE, 10000);

        Thread.sleep(2000);

        assertTrue(webTranslationProcessingResponse.getUsername().equals("username"));
        assertTrue(webTranslationProcessingResponse.getBookMetaDataId().equals(bookMetaData.getId()));
        assertTrue(webTranslationProcessingResponse.getTranslationResultDTO().getTranslation1().equals("test1."));
        assertTrue(webTranslationProcessingResponse.getTranslationResultDTO().getTranslation2().equals("test2."));

        Thread.sleep(2000);
        webTranslationProcessingResponse = (WebTranslationProcessingDTO) rabbitTemplate.receiveAndConvert(TRANSLATION_RESPONSE, 10000);

        assertTrue(webTranslationProcessingResponse.getUsername().equals("username"));
        assertTrue(webTranslationProcessingResponse.getBookMetaDataId().equals(bookMetaData.getId()));
        assertTrue(webTranslationProcessingResponse.getTranslationResultDTO().getTranslation1().equals("test1."));
        assertTrue(webTranslationProcessingResponse.getTranslationResultDTO().getTranslation2().equals("test2."));

        var word = webTranslationProcessingResponse.getTranslationResultDTO().getWords().stream().findFirst().get();
        assertTrue(word.getWord().equals("word1"));
        assertTrue(word.getTranslation().equals("translation1"));
        assertTrue(word.getTranscription().equals("[transcription1]"));
    }

    @Test
    public void translateWhileOutOfTokensTest() throws InterruptedException, IOException {
        GptTestUtils gptTestUtils = new GptTestUtils(appUserService, bookStockService);
        AppUser appUser = gptTestUtils.createAppUser("usernamenotokens");
        BookMetaData bookMetaData = gptTestUtils.createBookForAppUser(appUser);
        gptTestUtils.setZeroTokensToAppUser(appUser);

        WebTranslationProcessingDTO webTranslationProcessingDTO = new WebTranslationProcessingDTO(appUser.getUsername(), appUser.getId(), "test", bookMetaData.getId(), null);

        rabbitTemplate.convertAndSend(TRANSLATION_REQUEST, webTranslationProcessingDTO);

        WebTranslationProcessingDTO webTranslationProcessingResponse = (WebTranslationProcessingDTO) rabbitTemplate.receiveAndConvert(TRANSLATION_RESPONSE, 10000);

        Thread.sleep(2000);

        assertTrue(webTranslationProcessingResponse.getUsername().equals("usernamenotokens"));
        assertTrue(webTranslationProcessingResponse.getBookMetaDataId().equals(bookMetaData.getId()));
        assertTrue(webTranslationProcessingResponse.getTranslationResultDTO().getTranslation1().equals("You are out of tokens balance. =("));
        assertTrue(webTranslationProcessingResponse.getTranslationResultDTO().getTranslation2().equals(""));
    }
}
