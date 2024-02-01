package com.readnocry.integrationTest;

import com.readnocry.dto.MailParamsDTO;
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

import static com.readnocry.RabbitQueue.SEND_MAIL_REQUEST;
import static com.readnocry.dto.enums.MailPurpose.ACCOUNT_ACTIVATION;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {RabbitMqTestConfig.class, DBTestConfig.class})
public class CommandIntegrationTest {

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
    public void happyPathTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/command/send-me-mail-again"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        MailParamsDTO mailParamsDTO = (MailParamsDTO) rabbitTemplate.receiveAndConvert(SEND_MAIL_REQUEST, 10000);

        Thread.sleep(2000);

        assertTrue(mailParamsDTO.getMailPurpose().equals(ACCOUNT_ACTIVATION));
        assertTrue(mailParamsDTO.getEmailTo().equals("mihail@gmail.com"));
    }
}
