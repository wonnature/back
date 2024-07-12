package com.wonkwang.wonnature.repository;

import com.wonkwang.wonnature.domain.Board;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @EntityGraph(attributePaths = {"imageUrls"})
    Optional<Board> findByPathName(String pathname);
}
