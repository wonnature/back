package com.wonkwang.wonnature.repository;

import com.wonkwang.wonnature.domain.Notice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @EntityGraph(attributePaths = {"fileUrls"})
    Optional<Notice> findNoticeWithUrlsById(Long id);
}
