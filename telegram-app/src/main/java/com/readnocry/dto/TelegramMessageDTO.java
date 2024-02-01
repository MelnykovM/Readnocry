package com.readnocry.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessageDTO {

    private Long telegramChatId;
    private Integer telegramMessageId;
}
