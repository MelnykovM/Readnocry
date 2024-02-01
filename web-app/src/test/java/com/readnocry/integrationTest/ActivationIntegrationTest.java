package com.readnocry.integrationTest;

import com.readnocry.CryptoTool;
import com.readnocry.entity.AppUser;
import com.readnocry.service.AppUserService;
import com.readnocry.service.BookStockService;
import com.readnocry.testConfiguration.RabbitMqTestConfig;
import com.readnocry.testUtils.WebTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {RabbitMqTestConfig.class})
public class ActivationIntegrationTest {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private BookStockService bookStockService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CryptoTool cryptoTool;

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
    public void happyPathTest() throws Exception {
        WebTestUtils webTestUtils = new WebTestUtils(appUserService, bookStockService);
        AppUser appUser = webTestUtils.createAppUser("usernameActivation");
        assertFalse(appUserService.findById(appUser.getId()).get().getIsActive());

        String hashId = cryptoTool.hashOf(appUser.getId());

        mockMvc.perform(MockMvcRequestBuilders.get("/user/mail-activation")
                        .param("id", hashId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("accountActivationSuccess"));

        assertTrue(appUserService.findById(appUser.getId()).get().getIsActive());
    }
}
