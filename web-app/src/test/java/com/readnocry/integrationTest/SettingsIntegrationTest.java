package com.readnocry.integrationTest;

import com.readnocry.entity.AppUser;
import com.readnocry.entity.AppUserSettings;
import com.readnocry.entity.enums.Language;
import com.readnocry.entity.enums.LanguageProficiencyLevel;
import com.readnocry.entity.enums.PageSize;
import com.readnocry.entity.enums.PromptVersion;
import com.readnocry.service.AppUserService;
import com.readnocry.testConfiguration.DBTestConfig;
import com.readnocry.testConfiguration.RabbitMqTestConfig;
import org.junit.jupiter.api.Test;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {RabbitMqTestConfig.class, DBTestConfig.class})
public class SettingsIntegrationTest {

    @Autowired
    private AppUserService appUserService;

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
    public void registerAppUserTest() throws Exception {

        AppUser appUser = appUserService.findByUsername("mihail").orElseThrow();
        AppUserSettings appUserSettings = appUser.getAppUserSettings();

        mockMvc.perform(MockMvcRequestBuilders.post("/saveSettings")
                        .with(csrf())
                        .param("id", appUserSettings.getId().toString())
                        .param("languageProficiencyLevel", "HIGH")
                        .param("translateTo", "UKRAINIAN")
                        .param("promptVersion", "V2")
                        .param("pageSize", "XXL")
                )
                .andExpect(redirectedUrl("/settings"));

        AppUserSettings newAppUserSettings = appUserService.findByUsername("mihail").orElseThrow().getAppUserSettings();

        assertTrue(newAppUserSettings.getPromptVersion().equals(PromptVersion.V2));
        assertTrue(newAppUserSettings.getLanguageProficiencyLevel().equals(LanguageProficiencyLevel.HIGH));
        assertTrue(newAppUserSettings.getTranslateTo().equals(Language.UKRAINIAN));
        assertTrue(newAppUserSettings.getPageSize().equals(PageSize.XXL));
    }
}
