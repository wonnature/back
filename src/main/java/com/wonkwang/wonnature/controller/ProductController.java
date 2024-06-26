package com.wonkwang.wonnature.controller;

import com.wonkwang.wonnature.domain.ProductType;
import com.wonkwang.wonnature.domain.Role;
import com.wonkwang.wonnature.dto.ProductDTO;
import com.wonkwang.wonnature.dto.ResponseDTO;
import com.wonkwang.wonnature.service.ProductService;
import com.wonkwang.wonnature.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.wonkwang.wonnature.dto.ResponseEntityBuilder.build;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final S3Service s3Service;

    @GetMapping("/{productId}")
    public ResponseEntity<ResponseDTO<ProductDTO>> getProduct(@PathVariable Long productId) {

        return build("글 불러오기 완료", OK, productService.getOneProduct(productId));
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> searchProduct(@RequestParam String keyword) {
        if (keyword.length() < 2) {
            return build("최소 2글자 이상 입력해주세요.", BAD_REQUEST, null);
        }
        return build(keyword + " : 검색 완료", OK, productService.searchProduct(keyword));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> getProductList(@RequestParam(defaultValue = "화장품") ProductType type) {

        List<ProductDTO> findProducts = productService.getProductListByType(type);

        return build("제품목록 불러오기 완료", OK, findProducts);
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<Long>> createProduct(@RequestBody ProductDTO productDTO, @SessionAttribute Role role) {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }
        Long createdProductId = productService.createProduct(productDTO);
        return build("글 생성 완료", OK, createdProductId);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ResponseDTO<Long>> updateProduct(@RequestBody ProductDTO productDTO, @PathVariable Long productId, @SessionAttribute Role role) {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }
        System.out.println("productDTO = " + productDTO);
        productService.updateProduct(productId, productDTO);
        return build("글 수정 완료", OK, productId);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ResponseDTO<Long>> deleteProduct(@PathVariable Long productId, @SessionAttribute Role role) {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }
        productService.deleteOneProduct(productId);
        return build("글 삭제 완료", OK, productId);
    }

    @PostMapping("/image")
    public ResponseEntity<ResponseDTO<List<String>>> uploadImages(@RequestParam("file") MultipartFile[] files, @SessionAttribute Role role) throws IOException {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }

        // 현재 날짜와 시간을 사용하여 폴더 이름 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String folderName = "product/" + LocalDateTime.now().format(formatter);

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = s3Service.uploadFile(folderName, file);
            imageUrls.add(url);
        }
        System.out.println("Uploaded files: " + imageUrls.size()); // 업로드된 파일 수 출력
        return build("이미지 업로드 성공",OK, imageUrls);
    }
}
