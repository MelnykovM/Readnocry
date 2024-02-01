package com.readnocry.integrationTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readnocry.entity.AppUser;
import com.readnocry.entity.BookMetaData;
import com.readnocry.service.AppUserService;
import com.readnocry.service.BookStockService;
import com.readnocry.testConfiguration.DBTestConfig;
import com.readnocry.testConfiguration.RabbitMqTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {RabbitMqTestConfig.class, DBTestConfig.class})
public class ReaderIntegrationTest {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private BookStockService bookStockService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    public void readFileTest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "This is the file content.".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload")
                        .file(file)
                        .with(csrf())
                        .param("bookTitle", "Test Book Title"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/bookshelf"));

        AppUser appUser = appUserService.findByUsername("mihail").orElseThrow();

        BookMetaData bookMetaData = appUser.getBooks().stream().findFirst().orElseThrow();

        mockMvc.perform(MockMvcRequestBuilders.get("/reader")
                        .with(csrf())
                        .param("bookId", bookMetaData.getId().toString()))
                .andExpect(view().name("bookPage"))
                .andExpect(model().attributeExists("bookMetaDataJson"))
                .andExpect(model().attribute("bookMetaDataJson", getBookMetaDataJson(bookMetaData)));

        Files.delete(Paths.get(bookMetaData.getFilePath()));
    }

    private String getBookMetaDataJson(BookMetaData bookMetaData) {
        try {
            return objectMapper.writeValueAsString(bookMetaData);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
