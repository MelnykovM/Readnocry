package com.readnocry.gpt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceTranslationDTO {

    private String translation1;
    private String translation2;
    @JsonProperty("words_combinations_and_phraseological_units_and_words_of_this_sentence")
    private List<WordTranslationDTO> words;
}
