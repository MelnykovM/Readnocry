package com.readnocry.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordDTO {

    private Long appUserId;
    private String wordId;
    private String word;
    private String transcription;
    private String translation;

    @Override
    public String toString() {
        return "WordDTO{" +
                "appUserId=" + appUserId +
                ", wordId='" + wordId + '\'' +
                ", word='" + word + '\'' +
                ", transcription='" + transcription + '\'' +
                ", translation='" + translation + '\'' +
                '}';
    }
}
