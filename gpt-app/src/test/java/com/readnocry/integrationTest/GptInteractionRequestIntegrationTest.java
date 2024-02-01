package com.readnocry.integrationTest;

import com.readnocry.dto.ChatGptInteractionDTO;
import com.readnocry.dto.enums.ChatGptCommand;
import com.readnocry.entity.AppUser;
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

import java.util.List;

import static com.readnocry.RabbitQueue.CHAT_GPT_REQUEST;
import static com.readnocry.RabbitQueue.CHAT_GPT_RESPONSE;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {RabbitMqTestConfig.class, PostgreSQLTestConfig.class})
public class GptInteractionRequestIntegrationTest {

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
        ChatGptMessageDTO chatGptMessageDTO = new ChatGptMessageDTO("USER", "Chat GPT answer!");
        ChatGptChoiceDTO chatGptChoiceDTO = new ChatGptChoiceDTO(0, chatGptMessageDTO);
        when(openaiRestTemplate.postForObject(eq(gptUrl), any(ChatGptRequestDTO.class), eq(ChatGptResponseDTO.class))).thenReturn(new ChatGptResponseDTO(List.of(chatGptChoiceDTO), usageDTO));
    }

    @Test
    public void happyPathTest() throws InterruptedException {
        GptTestUtils gptTestUtils = new GptTestUtils(appUserService, bookStockService);
        AppUser appUser = gptTestUtils.createAppUser("usernameInteraction");
        ChatGptInteractionDTO chatGptInteractionRequest = new ChatGptInteractionDTO(appUser.getId(), 1L, "Text request", false, null);
        rabbitTemplate.convertAndSend(CHAT_GPT_REQUEST, chatGptInteractionRequest);

        ChatGptInteractionDTO chatGptInteractionDTO = (ChatGptInteractionDTO) rabbitTemplate.receiveAndConvert(CHAT_GPT_RESPONSE, 10000);

        Thread.sleep(2000);

        assertTrue(chatGptInteractionDTO.getAppUserId().equals(appUser.getId()));
        assertTrue(chatGptInteractionDTO.getMessage().equals("Chat GPT answer!"));
    }

    @Test
    public void commandShowMessageHistoryTest() throws InterruptedException {
        GptTestUtils gptTestUtils = new GptTestUtils(appUserService, bookStockService);
        AppUser appUser = gptTestUtils.createAppUser("usernameMessageHistory");
        ChatGptInteractionDTO chatGptInteractionMessageRequest = new ChatGptInteractionDTO(appUser.getId(), 1L, "Text request", false, null);
        rabbitTemplate.convertAndSend(CHAT_GPT_REQUEST, chatGptInteractionMessageRequest);
        Thread.sleep(1000);
        rabbitTemplate.receiveAndConvert(CHAT_GPT_RESPONSE, 10000);
        ChatGptInteractionDTO chatGptInteractionRequest = new ChatGptInteractionDTO(appUser.getId(), 1L, null, true, ChatGptCommand.SHOW_CHAT_GPT_HISTORY);
        rabbitTemplate.convertAndSend(CHAT_GPT_REQUEST, chatGptInteractionRequest);

        ChatGptInteractionDTO chatGptInteractionDTO = (ChatGptInteractionDTO) rabbitTemplate.receiveAndConvert(CHAT_GPT_RESPONSE, 10000);

        Thread.sleep(2000);

        assertTrue(chatGptInteractionDTO.getAppUserId().equals(appUser.getId()));
        assertTrue(chatGptInteractionDTO.getMessage().equals("token sum = 42     [Text request, Chat GPT answer!]"));
    }

    @Test
    public void commandDeleteMessageHistoryTest() throws InterruptedException {
        GptTestUtils gptTestUtils = new GptTestUtils(appUserService, bookStockService);
        AppUser appUser = gptTestUtils.createAppUser("usernameDeleteMessageHistory");
        ChatGptInteractionDTO chatGptInteractionMessageRequest = new ChatGptInteractionDTO(appUser.getId(), 1L, "Text request", false, null);
        rabbitTemplate.convertAndSend(CHAT_GPT_REQUEST, chatGptInteractionMessageRequest);
        Thread.sleep(1000);
        rabbitTemplate.receiveAndConvert(CHAT_GPT_RESPONSE, 10000);
        ChatGptInteractionDTO chatGptInteractionRequest = new ChatGptInteractionDTO(appUser.getId(), 1L, null, true, ChatGptCommand.CLEAN_CHAT_GPT_HISTORY);
        rabbitTemplate.convertAndSend(CHAT_GPT_REQUEST, chatGptInteractionRequest);

        ChatGptInteractionDTO chatGptInteractionDTO = (ChatGptInteractionDTO) rabbitTemplate.receiveAndConvert(CHAT_GPT_RESPONSE, 10000);

        Thread.sleep(2000);

        assertTrue(chatGptInteractionDTO.getAppUserId().equals(appUser.getId()));
        assertTrue(chatGptInteractionDTO.getMessage().equals("History is cleaned."));

        chatGptInteractionRequest = new ChatGptInteractionDTO(appUser.getId(), 1L, null, true, ChatGptCommand.SHOW_CHAT_GPT_HISTORY);
        rabbitTemplate.convertAndSend(CHAT_GPT_REQUEST, chatGptInteractionRequest);

        chatGptInteractionDTO = (ChatGptInteractionDTO) rabbitTemplate.receiveAndConvert(CHAT_GPT_RESPONSE, 10000);

        Thread.sleep(2000);

        assertTrue(chatGptInteractionDTO.getAppUserId().equals(appUser.getId()));
        assertTrue(chatGptInteractionDTO.getMessage().equals("No history."));
    }

    @Test
    public void translateWhileOutOfTokensTest() throws InterruptedException {
        GptTestUtils gptTestUtils = new GptTestUtils(appUserService, bookStockService);
        AppUser appUser = gptTestUtils.createAppUser("usernamenotoken");
        gptTestUtils.setZeroTokensToAppUser(appUser);
        ChatGptInteractionDTO chatGptInteractionRequest = new ChatGptInteractionDTO(appUser.getId(), 1L, "Text request", false, null);
        rabbitTemplate.convertAndSend(CHAT_GPT_REQUEST, chatGptInteractionRequest);

        ChatGptInteractionDTO chatGptInteractionDTO = (ChatGptInteractionDTO) rabbitTemplate.receiveAndConvert(CHAT_GPT_RESPONSE, 10000);

        Thread.sleep(2000);

        assertTrue(chatGptInteractionDTO.getAppUserId().equals(appUser.getId()));
        assertTrue(chatGptInteractionDTO.getMessage().equals("You are out of tokens balance. =("));
    }
}
