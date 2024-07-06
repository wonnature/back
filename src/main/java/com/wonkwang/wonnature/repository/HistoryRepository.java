package com.wonkwang.wonnature.repository;

import com.wonkwang.wonnature.domain.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {
}
