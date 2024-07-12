package com.wonkwang.wonnature.controller;

import com.wonkwang.wonnature.domain.Role;
import com.wonkwang.wonnature.dto.BoardDTO;
import com.wonkwang.wonnature.dto.ResponseDTO;
import com.wonkwang.wonnature.service.BoardService;
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
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final S3Service s3Service;

    @GetMapping
    public ResponseEntity<ResponseDTO<BoardDTO>> getBoard(@RequestParam String pathname) {

        return build(pathname + " 게시판 내용입니다.", OK, boardService.getBoard(pathname));
    }

    @PatchMapping
    public ResponseEntity<ResponseDTO<?>> updateBoard(@RequestParam String pathname,
                                                      @RequestBody BoardDTO boardDTO,
                                                      @SessionAttribute Role role) {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN);
        }

        System.out.println("pathname = " + pathname);
        boardService.updateBoard(boardDTO);
        return build(pathname + " 게시판 수정완료", OK);
    }

    @PostMapping("/image")
    public ResponseEntity<ResponseDTO<List<String>>> uploadImages(@RequestParam("file") MultipartFile[] files,
                                                                  @SessionAttribute Role role) throws IOException {
        if (role != Role.ADMIN) {
            return build("권한 부족", FORBIDDEN, null);
        }

        // 현재 날짜와 시간을 사용하여 폴더 이름 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String folderName = "board/" + LocalDateTime.now().format(formatter);

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = s3Service.uploadFile(folderName, file);
            imageUrls.add(url);
        }
        System.out.println("Uploaded files: " + imageUrls.size()); // 업로드된 파일 수 출력
        return build("이미지 업로드 성공",OK, imageUrls);
    }
}
