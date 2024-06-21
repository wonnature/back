package com.wonkwang.wonnature.repository;

import com.wonkwang.wonnature.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"imageUrls"})
    Optional<Product> findProductWithUrlsById(Long id);
    List<Product> findByTitleContaining(String keyword);

    @EntityGraph(attributePaths = {"imageUrls"})
    @Query("SELECT p FROM Product p")
    Page<Product> findAllWithImageUrls(Pageable pageable);

}
