package com.readnocry.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebTranslationProcessingDTO {

    private String username;
    private Long appUserId;
    private String sentence;
    Long bookMetaDataId;
    private TranslationResultDTO translationResultDTO;

    @Override
    public String toString() {
        return "WebTranslationProcessingDTO{" +
                "username='" + username + '\'' +
                ", appUserId=" + appUserId +
                ", sentence='" + sentence + '\'' +
                ", bookMetaDataId=" + bookMetaDataId +
                ", translationResultDTO=" + translationResultDTO +
                '}';
    }
}
