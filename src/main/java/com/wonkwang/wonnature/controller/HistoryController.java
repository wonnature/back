package com.wonkwang.wonnature.controller;

import com.wonkwang.wonnature.domain.Role;
import com.wonkwang.wonnature.dto.HistoryDTO;
import com.wonkwang.wonnature.dto.ResponseDTO;
import com.wonkwang.wonnature.dto.ResponseEntityBuilder;
import com.wonkwang.wonnature.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.wonkwang.wonnature.dto.ResponseEntityBuilder.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<ResponseDTO<HistoryDTO>> getHistory() {
        HistoryDTO findHistoryDTO = historyService.getHistory();

        return build("연혁 데이터입니다.", HttpStatus.OK, findHistoryDTO);

    }

    @PatchMapping
    public ResponseEntity<ResponseDTO<?>> updateHistory(@RequestBody HistoryDTO historyDTO, @SessionAttribute Role role) {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN);
        }

        historyService.updateHistory(historyDTO);

        return build("연혁 수정을 완료했습니다.", HttpStatus.OK);
    }
}
