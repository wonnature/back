package com.wonkwang.wonnature.controller;

import com.wonkwang.wonnature.domain.Role;
import com.wonkwang.wonnature.dto.PhotoGalleryDTO;
import com.wonkwang.wonnature.dto.ResponseDTO;
import com.wonkwang.wonnature.service.PhotoGalleryService;
import com.wonkwang.wonnature.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.wonkwang.wonnature.dto.ResponseEntityBuilder.build;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/photo-gallery")
public class PhotoGalleryController {

    private final PhotoGalleryService photoGalleryService;
    private final S3Service s3Service;

    @GetMapping("/{photoGalleryId}")
    public ResponseEntity<ResponseDTO<PhotoGalleryDTO>> getPhotoGallery(@PathVariable Long photoGalleryId) {

        return build("포토갤러리 불러오기 완료", OK, photoGalleryService.getOnePhotoGallery(photoGalleryId));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<PhotoGalleryDTO>>> getPhotoGalleryList() {

        List<PhotoGalleryDTO> photoGalleryList = photoGalleryService.getPhotoGalleryList();

        return build("포토갤러리 목록 불러오기 완료", OK, photoGalleryList);
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<Long>> createPhotoGallery(@RequestBody PhotoGalleryDTO photoGalleryDTO, @SessionAttribute Role role) {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }
        Long photoGalleryId = photoGalleryService.createPhotoGallery(photoGalleryDTO);
        return build("포토갤러리 생성 완료", OK, photoGalleryId);
    }

    @PatchMapping("/{photoGalleryId}")
    public ResponseEntity<ResponseDTO<Long>> updatePhotoGallery(@RequestBody PhotoGalleryDTO photoGalleryDTO, @PathVariable Long photoGalleryId, @SessionAttribute Role role) {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }
        System.out.println("photoGalleryDTO = " + photoGalleryDTO);
        photoGalleryService.updatePhotoGallery(photoGalleryId, photoGalleryDTO);
        return build("포토갤러리 수정 완료", OK, photoGalleryId);
    }

    @DeleteMapping("/{photoGalleryId}")
    public ResponseEntity<ResponseDTO<Long>> deleteProduct(@PathVariable Long photoGalleryId, @SessionAttribute Role role) {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }
        photoGalleryService.deleteOnePhotoGallery(photoGalleryId);
        return build("포토갤러리 삭제 완료", OK, photoGalleryId);
    }

    @PostMapping("/image")
    public ResponseEntity<ResponseDTO<List<String>>> uploadImages(@RequestParam("file") MultipartFile[] files, @SessionAttribute Role role) throws IOException {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }

        // 현재 날짜와 시간을 사용하여 폴더 이름 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String folderName = "photoGallery/" + LocalDateTime.now().format(formatter);

        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = s3Service.uploadFile(folderName, file);
            fileUrls.add(url);
        }
        System.out.println("Uploaded files: " + fileUrls.size()); // 업로드된 파일 수 출력
        return build("파일 업로드 성공",OK, fileUrls);
    }
}
