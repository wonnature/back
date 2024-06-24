package com.wonkwang.wonnature.controller;

import com.wonkwang.wonnature.domain.ProductType;
import com.wonkwang.wonnature.domain.Role;
import com.wonkwang.wonnature.dto.NoticeDTO;
import com.wonkwang.wonnature.dto.ProductDTO;
import com.wonkwang.wonnature.dto.ResponseDTO;
import com.wonkwang.wonnature.service.NoticeService;
import com.wonkwang.wonnature.service.ProductService;
import com.wonkwang.wonnature.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.wonkwang.wonnature.dto.ResponseEntityBuilder.build;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;
    private final S3Service s3Service;

    @GetMapping("/{noticeId}")
    public ResponseEntity<ResponseDTO<NoticeDTO>> getNotice(@PathVariable Long noticeId) {

        return build("공지 불러오기 완료", OK, noticeService.getOneNotice(noticeId));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<NoticeDTO>>> getNoticeList() {

        List<NoticeDTO> noticeList = noticeService.getNoticeList();

        return build("공지 목록 불러오기 완료", OK, noticeList);
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<Long>> createNotice(@RequestBody NoticeDTO noticeDTO, @SessionAttribute Role role) {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }
        Long noticeId = noticeService.createNotice(noticeDTO);
        return build("공지 생성 완료", OK, noticeId);
    }

    @PatchMapping("/{noticeId}")
    public ResponseEntity<ResponseDTO<Long>> updateNotice(@RequestBody NoticeDTO noticeDTO, @PathVariable Long noticeId, @SessionAttribute Role role) {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }
        System.out.println("noticeDTO = " + noticeDTO);
        noticeService.updateNotice(noticeId, noticeDTO);
        return build("공지 수정 완료", OK, noticeId);
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ResponseDTO<Long>> deleteProduct(@PathVariable Long noticeId, @SessionAttribute Role role) {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }
        noticeService.deleteOneNotice(noticeId);
        return build("공지 삭제 완료", OK, noticeId);
    }

    @PostMapping("/file")
    public ResponseEntity<ResponseDTO<List<String>>> uploadImages(@RequestParam("file") MultipartFile[] files, @SessionAttribute Role role) throws IOException {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = s3Service.uploadFile(file);
            imageUrls.add(url);
        }
        System.out.println("Uploaded files: " + imageUrls.size()); // 업로드된 파일 수 출력
        return build("파일 업로드 성공",OK, imageUrls);
    }
}
