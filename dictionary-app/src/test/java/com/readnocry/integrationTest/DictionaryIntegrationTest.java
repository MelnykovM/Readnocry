package com.readnocry.integrationTest;

import com.readnocry.dictionary.dao.DictionaryDao;
import com.readnocry.dictionary.dao.WordDao;
import com.readnocry.dictionary.service.DictionaryService;
import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.WordDTO;
import com.readnocry.testConfiguration.MongoDbTestConfig;
import com.readnocry.testConfiguration.RabbitMqTestConfig;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.readnocry.RabbitQueue.*;
import static org.junit.Assert.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {MongoDbTestConfig.class, RabbitMqTestConfig.class})
public class DictionaryIntegrationTest {

    @Autowired
    private DictionaryDao dictionaryDao;

    @Autowired
    private WordDao wordDao;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void happyPathTest() throws InterruptedException {
        DictionaryDTO dictionaryRequest = new DictionaryDTO("uesrname", 1L, null, null, null);
        rabbitTemplate.convertAndSend(GET_DICTIONARY_REQUEST, dictionaryRequest);

        DictionaryDTO dictionaryResponse = (DictionaryDTO) rabbitTemplate.receiveAndConvert(GET_DICTIONARY_RESPONSE, 10000);
        assertTrue(dictionaryResponse.getWords().isEmpty());

        Thread.sleep(2000);

        WordDTO wordDTO1 = new WordDTO(1L, null, "test1", "test1", "test1");
        WordDTO wordDTO2 = new WordDTO(1L, null, "test2", "test2", "test2");
        rabbitTemplate.convertAndSend(TRANSLATION_TO_DICTIONARY_REQUEST, wordDTO1);
        rabbitTemplate.convertAndSend(TRANSLATION_TO_DICTIONARY_REQUEST, wordDTO2);

        Thread.sleep(2000);

        rabbitTemplate.convertAndSend(GET_DICTIONARY_REQUEST, dictionaryRequest);

        dictionaryResponse = (DictionaryDTO) rabbitTemplate.receiveAndConvert(GET_DICTIONARY_RESPONSE, 10000);

        var wordFirst = dictionaryResponse.getWords().stream()
                .filter(w -> w.getWord().equals("test1")).findFirst().orElseThrow();
        rabbitTemplate.convertAndSend(DELETE_FROM_DICTIONARY_REQUEST, wordFirst);

        Thread.sleep(2000);

        rabbitTemplate.convertAndSend(GET_DICTIONARY_REQUEST, dictionaryRequest);
        dictionaryResponse = (DictionaryDTO) rabbitTemplate.receiveAndConvert(GET_DICTIONARY_RESPONSE, 10000);

        assertTrue(dictionaryResponse.getWords().stream().findFirst().orElseThrow().getWord().equals("test2"));

        var wordIds = dictionaryDao.findByAppUserId(1L).getWordIds();
        assertNotNull(wordIds);
        var word = wordDao.findById(wordIds.stream().findFirst().orElseThrow()).orElseThrow();
        Assert.assertEquals(word.getWord(), wordDTO2.getWord());
    }

    @Test
    public void addWordToUserDictionaryTest() {
        WordDTO wordDTO = new WordDTO(2L, null, "test", "test", "test");
        dictionaryService.addWordToUserDictionary(wordDTO);
        DictionaryDTO dictionaryRequest = new DictionaryDTO("uesrname", 2L, null, null, null);

        rabbitTemplate.convertAndSend(GET_DICTIONARY_REQUEST, dictionaryRequest);

        DictionaryDTO dictionaryResponse = (DictionaryDTO) rabbitTemplate.receiveAndConvert(GET_DICTIONARY_RESPONSE, 10000);
        System.out.println(dictionaryResponse);
        assertEquals(dictionaryResponse.getAppUserId(), wordDTO.getAppUserId());
        var wordIds = dictionaryDao.findByAppUserId(2L).getWordIds();
        assertNotNull(wordIds);
        var word = wordDao.findById(wordIds.stream().findFirst().orElseThrow()).orElseThrow();
        Assert.assertEquals(word.getWord(), wordDTO.getWord());
    }

    @Test
    public void deleteWordFromUserDictionaryTest() throws InterruptedException {
        WordDTO wordDTO = new WordDTO(3L, null, "test", "test", "test");
        dictionaryService.addWordToUserDictionary(wordDTO);
        DictionaryDTO dictionaryRequest = new DictionaryDTO("uesrname", 3L, null, null, null);

        Thread.sleep(2000);

        rabbitTemplate.convertAndSend(GET_DICTIONARY_REQUEST, dictionaryRequest);

        DictionaryDTO dictionaryResponse = (DictionaryDTO) rabbitTemplate.receiveAndConvert(GET_DICTIONARY_RESPONSE, 10000);
        System.out.println(dictionaryResponse);
        assertEquals(dictionaryResponse.getAppUserId(), wordDTO.getAppUserId());
        var wordIds = dictionaryDao.findByAppUserId(3L).getWordIds();
        assertNotNull(wordIds);
        var word = wordDao.findById(wordIds.stream().findFirst().orElseThrow()).orElseThrow();
        Assert.assertEquals(word.getWord(), wordDTO.getWord());
        wordDTO.setWordId(word.getId());

        Thread.sleep(2000);

        rabbitTemplate.convertAndSend(DELETE_FROM_DICTIONARY_REQUEST, wordDTO);

        Thread.sleep(2000);

        rabbitTemplate.convertAndSend(GET_DICTIONARY_REQUEST, dictionaryRequest);
        dictionaryResponse = (DictionaryDTO) rabbitTemplate.receiveAndConvert(GET_DICTIONARY_RESPONSE, 10000);
        assertTrue(dictionaryResponse.getWords().isEmpty());
    }
}
