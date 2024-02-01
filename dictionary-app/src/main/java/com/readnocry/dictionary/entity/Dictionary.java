package com.readnocry.dictionary.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Dictionary {

    @Id
    private String id;
    @Indexed
    private Long appUserId;
    private Set<String> wordIds;
    private LocalDateTime lastAction;

    @Override
    public String toString() {
        return "Dictionary{" +
                "id='" + id + '\'' +
                ", appUserId=" + appUserId +
                '}';
    }
}
