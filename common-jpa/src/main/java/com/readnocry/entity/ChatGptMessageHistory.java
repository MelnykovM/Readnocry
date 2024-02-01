package com.readnocry.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_message_history")
public class ChatGptMessageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private AppUser appUser;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ChatGptMessage> chatGptMessages;
    @CreationTimestamp
    private LocalDateTime startDate;
    private int tokenSum;

    public ChatGptMessageHistoryBuilder toBuilder(){
        return ChatGptMessageHistory.builder()
                .id(this.getId())
                .appUser(this.getAppUser())
                .startDate(this.getStartDate())
                .tokenSum(this.getTokenSum())
                .chatGptMessages(this.getChatGptMessages());
    }

    @Override
    public String toString() {
        return "ChatGptMessageHistory{" +
                "id=" + id +
                ", tokenSum=" + tokenSum +
                '}';
    }
}
