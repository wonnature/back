package com.wonkwang.wonnature.repository;

import com.wonkwang.wonnature.domain.Notice;
import com.wonkwang.wonnature.domain.PhotoGallery;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhotoGalleryRepository extends JpaRepository<PhotoGallery, Long> {

    @EntityGraph(attributePaths = {"imageUrls"})
    Optional<PhotoGallery> findPhotoGalleryWithUrlsById(Long id);
}
