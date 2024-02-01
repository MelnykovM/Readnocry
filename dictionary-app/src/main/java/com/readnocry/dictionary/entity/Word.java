package com.readnocry.dictionary.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Word {

    @Id
    private String id;
    private Long appUserId;
    private String word;
    private String transcription;
    private String translation;
    private LocalDateTime dateAdded;

    public Word(Long appUserId,
                String word,
                String transcription,
                String translation,
                LocalDateTime dateAdded) {
        this.appUserId = appUserId;
        this.word = word;
        this.transcription = transcription;
        this.translation = translation;
        this.dateAdded = dateAdded;
    }

    @Override
    public String toString() {
        return "Word{" +
                "id='" + id + '\'' +
                ", appUserId=" + appUserId +
                ", word='" + word + '\'' +
                ", transcription='" + transcription + '\'' +
                ", translation='" + translation + '\'' +
                '}';
    }
}
