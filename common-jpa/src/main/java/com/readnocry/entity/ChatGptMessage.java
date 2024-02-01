package com.readnocry.entity;

import com.readnocry.entity.enums.ChatMessageRole;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_message")
public class ChatGptMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    private ChatGptMessageHistory chatGptMessageHistory;
    @CreationTimestamp
    private LocalDateTime startDate;
    @Enumerated(EnumType.STRING)
    private ChatMessageRole role;
    @Column(length = 10000)
    private String content;

    public ChatGptMessageBuilder toBuilder(){
        return ChatGptMessage.builder()
                .id(this.id)
                .chatGptMessageHistory(this.chatGptMessageHistory)
                .startDate(this.startDate)
                .role(this.role)
                .content(this.content);
    }

    @Override
    public String toString() {
        return "ChatGptMessage{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}
