package com.readnocry.dto;

import com.readnocry.dto.enums.ChatGptCommand;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptInteractionDTO {

    private Long appUserId;
    private Long telegramChatId;
    private String message;
    private Boolean isCommand;
    private ChatGptCommand command;

    @Override
    public String toString() {
        return "ChatGptInteractionDTO{" +
                "appUserId=" + appUserId +
                ", telegramChatId=" + telegramChatId +
                ", message='" + message + '\'' +
                ", isCommand=" + isCommand +
                ", command=" + command +
                '}';
    }
}
