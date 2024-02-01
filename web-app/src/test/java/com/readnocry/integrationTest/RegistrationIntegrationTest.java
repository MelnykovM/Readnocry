package com.readnocry.integrationTest;

import com.readnocry.dto.MailParamsDTO;
import com.readnocry.entity.AppUser;
import com.readnocry.service.AppUserService;
import com.readnocry.testConfiguration.DBTestConfig;
import com.readnocry.testConfiguration.RabbitMqTestConfig;
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

import static com.readnocry.RabbitQueue.SEND_MAIL_REQUEST;
import static com.readnocry.dto.enums.MailPurpose.ACCOUNT_ACTIVATION;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {RabbitMqTestConfig.class, DBTestConfig.class})
public class RegistrationIntegrationTest {

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
    public void registerAppUserTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("password", "password")
                        .param("email", "newuser@example.com"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        AppUser appUser = appUserService.findByUsername("newuser").orElseThrow();

        assertTrue(appUser.getUsername().equals("newuser"));
        assertTrue(appUser.getEmail().equals("newuser@example.com"));

        MailParamsDTO mailParamsDTO = (MailParamsDTO) rabbitTemplate.receiveAndConvert(SEND_MAIL_REQUEST, 10000);

        Thread.sleep(2000);

        assertTrue(mailParamsDTO.getMailPurpose().equals(ACCOUNT_ACTIVATION));
        assertTrue(mailParamsDTO.getEmailTo().equals("newuser@example.com"));
    }
}
