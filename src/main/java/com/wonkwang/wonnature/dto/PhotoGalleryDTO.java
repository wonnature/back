package com.wonkwang.wonnature.dto;

import com.wonkwang.wonnature.domain.Notice;
import com.wonkwang.wonnature.domain.PhotoGallery;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class PhotoGalleryDTO {

    private Long id;
    private Long hit = 0L; //조회수
    private String title;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public PhotoGalleryDTO(PhotoGallery photoGallery) {
        id = photoGallery.getId();
        hit = photoGallery.getHit();
        title = photoGallery.getTitle();
        content = photoGallery.getContent();
        imageUrls = photoGallery.getImageUrls();
        createdDate = photoGallery.getCreatedDate();
        lastModifiedDate = photoGallery.getLastModifiedDate();
    }
}
