package com.readnocry.testConfiguration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.testcontainers.containers.MongoDBContainer;

@TestConfiguration
@EnableMongoRepositories(basePackages = "com.readnocry.dictionary.dao")
public class MongoDbTestConfig {

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    static {
        mongoDBContainer.start();
    }

    @Bean
    public MongoClient mongoClient() {
        String host = mongoDBContainer.getContainerIpAddress();
        int port = mongoDBContainer.getFirstMappedPort();
        return MongoClients.create(String.format("mongodb://%s:%d", host, port));
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, "OAuth2Sample");
    }
}