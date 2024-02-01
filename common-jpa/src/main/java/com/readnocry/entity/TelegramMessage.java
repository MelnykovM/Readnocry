package com.readnocry.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "telegram_message")
public class TelegramMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private TelegramAppUser appUser;
    private Long telegramChatId;
    private Integer telegramMessageId;

    @Override
    public String toString() {
        return "TelegramMessage{" +
                "id=" + id +
                ", telegramChatId=" + telegramChatId +
                ", telegramMessageId=" + telegramMessageId +
                '}';
    }
}
