package com.wonkwang.wonnature.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private Long hit = 0L; // 조회수

    private String title;

    @Lob
    private String content;

    @ElementCollection
    @OrderColumn(name = "file_url_order")
    private List<String> fileUrls = new ArrayList<>();

    public void addHit() {
        hit += 1;
    }

    public void updateNotice(String title, String content, List<String> fileUrls) {
        this.title = title;
        this.content = content;
        this.fileUrls = fileUrls;
    }
}
