package com.readnocry.gpt.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptResponseDTO {

    private List<ChatGptChoiceDTO> choices;
    private UsageDTO usage;
}
