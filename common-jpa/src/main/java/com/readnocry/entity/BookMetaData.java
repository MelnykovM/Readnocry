package com.readnocry.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"id", "appUser"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book_meta_data")
public class BookMetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_user_id")
    @JsonBackReference
    private AppUser appUser;
    private String fileName;
    private String bookTitle;
    private String filePath;
    private int page;
    private int pageSize;
    private String charset;

    @Override
    public String toString() {
        return "BookMetaData{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", bookTitle='" + bookTitle + '\'' +
                ", filePath='" + filePath + '\'' +
                ", page=" + page +
                ", pageSize=" + pageSize +
                '}';
    }
}
