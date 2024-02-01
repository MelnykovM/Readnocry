package com.readnocry.testConfiguration;

import com.readnocry.entity.AppUser;
import com.readnocry.service.AppUserService;
import com.readnocry.service.BookStockService;
import com.readnocry.testUtils.WebTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

@TestConfiguration
@ComponentScan(basePackages = "com.readnocry")
public class DBTestConfig {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private BookStockService bookStockService;

    @Autowired
    private WebTestUtils webTestUtils;

    @Bean
    public WebTestUtils webTestUtils() {
        return new WebTestUtils(appUserService, bookStockService);
    }

    @PostConstruct
    public void setupUser() {
        AppUser appUser = webTestUtils.createAppUser("mihail");
    }
}
