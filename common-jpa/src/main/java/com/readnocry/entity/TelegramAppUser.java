package com.readnocry.entity;

import com.readnocry.entity.enums.TelegramAppUserState;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "telegram_user")
public class TelegramAppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramUserId;
    private Long telegramChatId;
    @CreationTimestamp
    private LocalDateTime firstLoginDate;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    @Enumerated(EnumType.STRING)
    private TelegramAppUserState state;
    private Boolean connectedToAppUser;
    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TelegramMessage> messages = new ArrayList<>();

    @Override
    public String toString() {
        return "TelegramAppUser{" +
                "id=" + id +
                ", telegramUserId=" + telegramUserId +
                ", telegramChatId=" + telegramChatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", state=" + state +
                ", connectedToAppUser=" + connectedToAppUser +
                '}';
    }
}
