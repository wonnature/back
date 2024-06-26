package com.wonkwang.wonnature.dto;

import com.wonkwang.wonnature.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDTO {

    private Long id;
    private Long hit = 0L; //조회수
    private String title;
    private String content;
    private List<String> fileUrls;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public NoticeDTO(Notice notice) {
        id = notice.getId();
        hit = notice.getHit();
        title = notice.getTitle();
        content = notice.getContent();
        fileUrls = notice.getFileUrls();
        createdDate = notice.getCreatedDate();
        lastModifiedDate = notice.getLastModifiedDate();
    }
}
