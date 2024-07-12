package com.wonkwang.wonnature.domain;

import com.wonkwang.wonnature.dto.BoardDTO;
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
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Builder.Default
    private Boolean isContentTop = false;

    private String pathName;

    @ElementCollection
    @OrderColumn(name = "image_url_order")
    private List<String> imageUrls = new ArrayList<>();

    public Board(BoardDTO boardDTO) {
        createEntity(boardDTO);
    }
    public void updateBoard(BoardDTO boardDTO) {
        createEntity(boardDTO);
    }

    private void createEntity(BoardDTO boardDTO) {
        content = boardDTO.getContent();
        isContentTop = boardDTO.getIsContentTop();
        imageUrls = boardDTO.getImageUrls();
        pathName = boardDTO.getPathName();
    }
}
