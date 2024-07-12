package com.wonkwang.wonnature.dto;

import com.wonkwang.wonnature.domain.Board;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {

    private Long id;
    private String content;
    private Boolean isContentTop;
    private String pathName;
    private List<String> imageUrls;

    public BoardDTO(Board board) {
        id = board.getId();
        content = board.getContent();
        isContentTop = board.getIsContentTop();
        pathName = board.getPathName();
        imageUrls = board.getImageUrls();
    }
}
