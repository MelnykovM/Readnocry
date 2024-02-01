package com.readnocry.gpt.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptChoiceDTO {

    private int index;
    private ChatGptMessageDTO message;
}
