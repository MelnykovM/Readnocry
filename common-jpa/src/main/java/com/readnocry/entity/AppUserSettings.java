package com.readnocry.entity;

import com.readnocry.entity.enums.Language;
import com.readnocry.entity.enums.LanguageProficiencyLevel;
import com.readnocry.entity.enums.PageSize;
import com.readnocry.entity.enums.PromptVersion;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_user_settings")
public class AppUserSettings {

    @Id
    private Long id;
    LanguageProficiencyLevel languageProficiencyLevel;
    Language translateTo;
    PromptVersion promptVersion;
    PageSize pageSize;
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private AppUser appUser;

    @Override
    public String toString() {
        return "AppUserSettings{" +
                "id=" + id +
                ", languageProficiencyLevel=" + languageProficiencyLevel +
                ", translateTo=" + translateTo +
                ", promptVersion=" + promptVersion +
                ", pageSize=" + pageSize +
                '}';
    }
}
