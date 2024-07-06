package com.wonkwang.wonnature.dto;

import com.wonkwang.wonnature.domain.History;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryDTO {

    private Long id;
    private String content;

    public HistoryDTO(History history) {
        id = history.getId();
        content = history.getContent();
    }
}
