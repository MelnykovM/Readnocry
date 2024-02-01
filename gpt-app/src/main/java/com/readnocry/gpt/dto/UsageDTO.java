package com.readnocry.gpt.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageDTO {

    int total_tokens;
}
