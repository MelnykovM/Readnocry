package com.readnocry.dictionary.service.impl;

import com.readnocry.dictionary.dao.DictionaryDao;
import com.readnocry.dictionary.dao.WordDao;
import com.readnocry.dictionary.entity.Dictionary;
import com.readnocry.dictionary.entity.Word;
import com.readnocry.dictionary.service.DictionaryService;
import com.readnocry.dto.DictionaryDTO;
import com.readnocry.dto.WordDTO;
import com.readnocry.rabbitmq.service.DictionaryRabbitProducer;
import lombok.extern.log4j.Log4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Log4j
public class DictionaryServiceImpl implements DictionaryService {

    private final MongoTemplate mongoTemplate;
    private final DictionaryDao dictionaryDao;
    private final DictionaryRabbitProducer dictionaryRabbitProducer;
    private final WordDao wordDao;

    public DictionaryServiceImpl(MongoTemplate mongoTemplate,
                                 DictionaryDao dictionaryDao,
                                 DictionaryRabbitProducer dictionaryRabbitProducer,
                                 WordDao wordDao) {
        this.mongoTemplate = mongoTemplate;
        this.dictionaryDao = dictionaryDao;
        this.dictionaryRabbitProducer = dictionaryRabbitProducer;
        this.wordDao = wordDao;
    }

    @Transactional
    public void addWordToUserDictionary(WordDTO wordDTO) {
        log.info("Adding word to user dictionary: " + wordDTO);
        Word word = Word.builder()
                .appUserId(wordDTO.getAppUserId())
                .word(wordDTO.getWord())
                .transcription(wordDTO.getTranscription())
                .translation(wordDTO.getTranslation())
                .dateAdded(LocalDateTime.now())
                .build();
        Word savedWord = wordDao.save(word);
        com.readnocry.dictionary.entity.Dictionary userDictionary = dictionaryDao.findByAppUserId(wordDTO.getAppUserId());
        if (userDictionary == null) {
            userDictionary = new com.readnocry.dictionary.entity.Dictionary();
            userDictionary.setAppUserId(wordDTO.getAppUserId());
            userDictionary.setWordIds(new HashSet<>());
            userDictionary.setLastAction(LocalDateTime.now());
            userDictionary = dictionaryDao.save(userDictionary);
        }
        String wordId = savedWord.getId();
        addWordIdToDictionary(userDictionary.getId(), wordId);
        log.info("Word successfully added: " + wordDTO);
    }

    @Override
    public void consumeGetDictionaryRequest(DictionaryDTO dictionary) {
        log.info("Consuming get dictionary request: " + dictionary);
        Set<WordDTO> words = getWordsForUser(dictionary.getAppUserId()).stream()
                .map(w -> WordDTO.builder()
                        .wordId(w.getId())
                        .appUserId(w.getAppUserId())
                        .word(w.getWord())
                        .translation(w.getTranslation())
                        .transcription(w.getTranscription())
                        .build())
                .collect(Collectors.toSet());
        dictionary.setWords(words);
        dictionaryRabbitProducer.produceGetDictionaryResponse(dictionary);
    }

    @Override
    @Transactional
    public void deleteFromDictionary(WordDTO request) {
        log.info("Deleting word from dictionary: " + request);
        com.readnocry.dictionary.entity.Dictionary userDictionary = dictionaryDao.findByAppUserId(request.getAppUserId());
        if (userDictionary != null && userDictionary.getWordIds().contains(request.getWordId())) {
            Set<String> wordIds = userDictionary.getWordIds();
            wordIds.remove(request.getWordId());
            userDictionary.setWordIds(wordIds);
            dictionaryDao.save(userDictionary);
            wordDao.deleteById(request.getWordId());
            log.info("Word successfully deleted: " + request);
        }
    }

    public Set<Word> getWordsForUser(Long userId) {
        log.info("Retrieving words for user: " + userId);
        com.readnocry.dictionary.entity.Dictionary userDictionary = dictionaryDao.findByAppUserId(userId);
        if (userDictionary == null) {
            return Set.of();
        }
        Set<String> wordIds = userDictionary.getWordIds();
        Iterable<Word> wordsIterable = wordDao.findAllById(wordIds);
        Set<Word> wordsList = new HashSet<>();
        wordsIterable.forEach(wordsList::add);
        return wordsList;
    }

    public void addWordIdToDictionary(String dictionaryId, String wordId) {
        log.info("Adding wordId to dictionary: " + dictionaryId + ", WordId: " + wordId);
        Query query = new Query(Criteria.where("id").is(dictionaryId));
        Update update = new Update().addToSet("wordIds", wordId).set("lastAction", LocalDateTime.now());
        mongoTemplate.updateFirst(query, update, Dictionary.class);
    }
}
