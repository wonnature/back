package com.wonkwang.wonnature.service;

import com.wonkwang.wonnature.domain.Product;
import com.wonkwang.wonnature.domain.ProductType;
import com.wonkwang.wonnature.dto.ProductDTO;
import com.wonkwang.wonnature.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final S3Service s3Service;
    private final DiscordService discordService;

    public Long createProduct(ProductDTO productDTO) {
        String sanitizedContent = sanitize(productDTO.getContent()); //악성 스크립트 제거
        productDTO.setContent(sanitizedContent);
        Product post = new Product(productDTO);
        Product savedProduct = productRepository.save(post);

        discordService.sendActivityMessage(productDTO.toString() + " : 제품을 등록했습니다.");
        log.info("{} : 제품을 등록했습니다.", productDTO.toString());

        return savedProduct.getId();
    }
    @Transactional
    public void updateProduct(Long postId, ProductDTO productDTO) {
        Product findProduct = productRepository.findProductWithUrlsById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 글이 없습니다."));

        String sanitizedContent = sanitize(productDTO.getContent()); //악성 스크립트 제거
        productDTO.setContent(sanitizedContent);

        // 기존 이미지 URL 리스트
        List<String> existingImageUrls = findProduct.getImageUrls();

        // 새로운 이미지 URL 리스트
        List<String> newImageUrls = productDTO.getImageUrls();

        // 삭제된 이미지 URL 리스트
        List<String> deletedImageUrls = existingImageUrls.stream()
                .filter(url -> !newImageUrls.contains(url))
                .toList();

        // S3에서 삭제 요청
        deletedImageUrls.forEach(s3Service::deleteFile);

        findProduct.updateProduct(productDTO);

        discordService.sendActivityMessage(productDTO.toString() + " : 제품을 수정했습니다.");
        log.info("{} 글 수정 완료", postId);
    }

    @Transactional
    public ProductDTO getOneProduct(Long postId) {
        Product findProduct = productRepository.findProductWithUrlsById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 글이 없습니다."));
        findProduct.addHit();

        return new ProductDTO(findProduct);
    }

    public List<ProductDTO> searchProduct(String keyword) {
        List<Product> searchList = productRepository.findByTitleContaining(keyword);
        return searchList.stream().map(ProductDTO::new).toList();
    }

    public Page<ProductDTO> getProductList(Pageable pageable) {
        Page<Product> findProducts = productRepository.findAllWithImageUrls(pageable);
        return findProducts.map(ProductDTO::new);
    }

    public List<ProductDTO> getProductListByType(ProductType productType) {
        List<Product> findProducts = productRepository.findByProductType(productType);
        return findProducts.stream().map(ProductDTO::new).toList();
    }

    public void deleteOneProduct(Long postId) {
        Product findProduct = productRepository.findProductWithUrlsById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 글이 없습니다."));

        List<String> imageUrls = findProduct.getImageUrls();
        imageUrls.forEach(s3Service::deleteFile);

        productRepository.delete(findProduct);

        discordService.sendActivityMessage(postId + " : 제품을 삭제했습니다.");
        log.info("{} 글 삭제 완료", postId);
    }

    private String sanitize(String content) {
        // 정규 표현식을 사용하여 여러 태그와 속성을 제거
        String cleanContent = content.replaceAll("(?i)<script.*?>.*?</script.*?>", ""); // script 태그 제거
        cleanContent = cleanContent.replaceAll("(?i)<img.*?>", "[removed]"); // img 태그 제거
        cleanContent = cleanContent.replaceAll("(?i)\\s+on\\w+\\s*=\\s*\"[^\"]*\"", ""); // 이벤트 핸들러 속성 제거
        cleanContent = cleanContent.replaceAll("(?i)\\s+on\\w+\\s*=\\s*'[^']*'", ""); // 이벤트 핸들러 속성 제거 (단일 인용부호)
        cleanContent = cleanContent.replaceAll("(?i)\\s+javascript\\s*:\\s*[^\"]*\"", ""); // javascript: 속성 제거
        cleanContent = cleanContent.replaceAll("(?i)\\s+javascript\\s*:\\s*[^']*'", ""); // javascript: 속성 제거 (단일 인용부호)
        return cleanContent;
    }
}

