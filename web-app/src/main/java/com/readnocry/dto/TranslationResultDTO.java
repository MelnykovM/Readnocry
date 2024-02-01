package com.readnocry.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResultDTO {

    private String translation1;
    private String translation2;
    private List<WordDTO> words;

    @Override
    public String toString() {
        return "TranslationResultDTO{" +
                "translation1='" + translation1 + '\'' +
                ", translation2='" + translation2 + '\'' +
                ", words=" + words +
                '}';
    }
}
