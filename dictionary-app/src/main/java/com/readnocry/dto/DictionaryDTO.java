package com.readnocry.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryDTO {

    private String username;
    private Long appUserId;
    private LocalDateTime fromTime;
    private LocalDateTime toTime;
    private Set<WordDTO> words;

    @Override
    public String toString() {
        return "DictionaryDTO{" +
                "username='" + username + '\'' +
                ", appUserId=" + appUserId +
                '}';
    }
}
