package com.readnocry.dto;

import com.readnocry.dto.enums.MailPurpose;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailParamsDTO {

    private String id;
    private String emailTo;
    private MailPurpose mailPurpose;

    @Override
    public String toString() {
        return "MailParamsDTO{" +
                "id='" + id + '\'' +
                ", emailTo='" + emailTo + '\'' +
                ", mailPurpose=" + mailPurpose +
                '}';
    }
}
