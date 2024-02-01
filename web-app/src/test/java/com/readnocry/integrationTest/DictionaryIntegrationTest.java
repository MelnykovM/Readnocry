package com.readnocry.integrationTest;

import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.WordDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.service.AppUserService;
import com.readnocry.testConfiguration.DBTestConfig;
import com.readnocry.testConfiguration.RabbitMqTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.readnocry.RabbitQueue.*;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {RabbitMqTestConfig.class, DBTestConfig.class})
public class DictionaryIntegrationTest {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Test
    @WithUserDetails(value = "mihail", userDetailsServiceBeanName = "appUserServiceImpl")
    public void addWordTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/add-to-dictionary")
                        .with(csrf())
                        .param("word", "word1")
                        .param("transcription", "transcription1")
                        .param("translation", "translation1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        WordDTO wordDTO = (WordDTO) rabbitTemplate.receiveAndConvert(TRANSLATION_TO_DICTIONARY_REQUEST, 10000);

        Thread.sleep(2000);

        AppUser appUser = appUserService.findByUsername("mihail").orElseThrow();

        assertTrue(wordDTO.getAppUserId().equals(appUser.getId()));
        assertTrue(wordDTO.getWord().equals("word1"));
        assertTrue(wordDTO.getTranscription().equals("transcription1"));
        assertTrue(wordDTO.getTranslation().equals("translation1"));
    }

    @Test
    @WithUserDetails(value = "mihail", userDetailsServiceBeanName = "appUserServiceImpl")
    public void deleteWordTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/delete-from-dictionary")
                        .with(csrf())
                        .param("wordId", "id1")
                        .param("word", "word1")
                        .param("transcription", "transcription1")
                        .param("translation", "translation1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        WordDTO wordDTO = (WordDTO) rabbitTemplate.receiveAndConvert(DELETE_FROM_DICTIONARY_REQUEST, 10000);

        Thread.sleep(2000);

        AppUser appUser = appUserService.findByUsername("mihail").orElseThrow();

        assertTrue(wordDTO.getAppUserId().equals(appUser.getId()));
        assertTrue(wordDTO.getWordId().equals("id1"));
        assertTrue(wordDTO.getWord().equals("word1"));
        assertTrue(wordDTO.getTranscription().equals("transcription1"));
        assertTrue(wordDTO.getTranslation().equals("translation1"));
    }

    @Test
    @WithUserDetails(value = "mihail", userDetailsServiceBeanName = "appUserServiceImpl")
    public void getDictionaryTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/get-my-dictionary"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        DictionaryDTO dictionaryDTO = (DictionaryDTO) rabbitTemplate.receiveAndConvert(GET_DICTIONARY_REQUEST, 10000);

        Thread.sleep(2000);

        AppUser appUser = appUserService.findByUsername("mihail").orElseThrow();

        assertTrue(dictionaryDTO.getAppUserId().equals(appUser.getId()));
    }
}
