package com.wonkwang.wonnature.service;

import com.wonkwang.wonnature.domain.History;
import com.wonkwang.wonnature.dto.HistoryDTO;
import com.wonkwang.wonnature.repository.HistoryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;

    @PostConstruct
    public void init() {

        List<History> all = historyRepository.findAll();
        if (all.isEmpty()) {
            historyRepository.save(new History(1L,"연혁 내용을 입력해주세요."));
        }
    }
    public HistoryDTO getHistory() {

        History findHistory = historyRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("연혁 데이터가 없습니다."));

        return new HistoryDTO(findHistory);
    }

    @Transactional
    public void updateHistory(HistoryDTO historyDTO) {

        History findHistory = historyRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("연혁 데이터가 없습니다."));

        findHistory.updateHistory(historyDTO.getContent());
    }
}
