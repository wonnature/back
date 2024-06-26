package com.wonkwang.wonnature.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoGallery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private Long hit = 0L; // 조회수

    private String title;

    @Lob
    private String content;

    @ElementCollection
    @OrderColumn(name = "image_url_order")
    private List<String> imageUrls = new ArrayList<>();

    public void addHit() {
        hit += 1;
    }

    public void updatePhotoGallery(String title, String content, List<String> imageUrls) {
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls;
    }
}
