package com.readnocry.gpt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordTranslationDTO {

    @JsonProperty("words_combination_or_phraseological_unit_or_word")
    String word;
    String transcription;
    String translation;
}
