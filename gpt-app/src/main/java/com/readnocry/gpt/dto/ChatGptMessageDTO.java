package com.readnocry.gpt.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptMessageDTO {

    private String role;
    private String content;
}
