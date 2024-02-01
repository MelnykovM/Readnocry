package com.readnocry.gpt.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptRequestDTO {

    private String model;
    private List<ChatGptMessageDTO> messages;
}
