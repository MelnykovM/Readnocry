package com.readnocry.unitTest;

import com.readnocry.dictionary.dao.DictionaryDao;
import com.readnocry.dictionary.dao.WordDao;
import com.readnocry.dictionary.entity.Dictionary;
import com.readnocry.dictionary.entity.Word;
import com.readnocry.dictionary.service.impl.DictionaryServiceImpl;
import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.WordDTO;
import com.readnocry.rabbitmq.service.DictionaryRabbitProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class DictionaryServiceImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private DictionaryDao dictionaryDao;

    @Mock
    private DictionaryRabbitProducer dictionaryRabbitProducer;

    @Mock
    private WordDao wordDao;

    private DictionaryServiceImpl dictionaryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dictionaryService = new DictionaryServiceImpl(mongoTemplate, dictionaryDao, dictionaryRabbitProducer, wordDao);
    }

    @Test
    void testAddWordToUserDictionary() {
        WordDTO wordDTO = new WordDTO();
        wordDTO.setAppUserId(1L);
        wordDTO.setWord("test");
        wordDTO.setTranscription("transcription");
        wordDTO.setTranslation("translation");

        when(wordDao.save(any(Word.class))).thenReturn(new Word());
        when(dictionaryDao.findByAppUserId(1L)).thenReturn(null);
        when(dictionaryDao.save(any(Dictionary.class))).thenReturn(new Dictionary());

        dictionaryService.addWordToUserDictionary(wordDTO);

        verify(wordDao, times(1)).save(any(Word.class));
        verify(dictionaryDao, times(1)).findByAppUserId(1L);
        verify(dictionaryDao, times(1)).save(any(Dictionary.class));
    }

    @Test
    void testConsumeGetDictionaryRequest() {
        DictionaryDTO dictionaryDTO = new DictionaryDTO();
        dictionaryDTO.setAppUserId(1L);

        when(dictionaryDao.findByAppUserId(1L)).thenReturn(new Dictionary());
        when(wordDao.findAllById(any())).thenReturn(Set.of(new Word()));

        dictionaryService.consumeGetDictionaryRequest(dictionaryDTO);

        verify(dictionaryDao, times(1)).findByAppUserId(1L);
        verify(wordDao, times(1)).findAllById(any());
        verify(dictionaryRabbitProducer, times(1)).produceGetDictionaryResponse(dictionaryDTO);
    }

    @Test
    void testDeleteFromDictionary() {
        WordDTO wordDTO = new WordDTO();
        wordDTO.setAppUserId(1L);
        wordDTO.setWordId("wordId");

        Dictionary userDictionary = new Dictionary();
        userDictionary.setAppUserId(1L);
        userDictionary.setWordIds(new HashSet<>());
        userDictionary.getWordIds().add("wordId");

        when(dictionaryDao.findByAppUserId(1L)).thenReturn(userDictionary);

        dictionaryService.deleteFromDictionary(wordDTO);

        verify(dictionaryDao, times(1)).findByAppUserId(1L);
        verify(wordDao, times(1)).deleteById("wordId");
    }

    @Test
    void testGetWordsForUser() {
        when(dictionaryDao.findByAppUserId(1L)).thenReturn(new Dictionary());
        when(wordDao.findAllById(any())).thenReturn(Set.of(new Word()));

        Set<Word> words = dictionaryService.getWordsForUser(1L);

        verify(dictionaryDao, times(1)).findByAppUserId(1L);
        verify(wordDao, times(1)).findAllById(any());
        assertEquals(1, words.size());
    }

    @Test
    void testAddWordIdToDictionary() {
        when(dictionaryDao.save(any(Dictionary.class))).thenReturn(new Dictionary());

        dictionaryService.addWordIdToDictionary("dictionaryId", "wordId");

        verify(mongoTemplate, times(1)).updateFirst(any(), any(), eq(Dictionary.class));
    }

    @Test
    void testDeleteFromDictionaryNotFound() {
        WordDTO wordDTO = new WordDTO();
        wordDTO.setAppUserId(1L);
        wordDTO.setWordId("wordId");

        when(dictionaryDao.findByAppUserId(1L)).thenReturn(new Dictionary("id", 1L, new HashSet<>(), null));

        dictionaryService.deleteFromDictionary(wordDTO);

        verify(dictionaryDao, times(1)).findByAppUserId(1L);
        verify(wordDao, never()).deleteById("wordId");
    }
}
