package com.wonkwang.wonnature.service;

import com.wonkwang.wonnature.domain.Notice;
import com.wonkwang.wonnature.dto.NoticeDTO;
import com.wonkwang.wonnature.repository.NoticeRepository;
import com.wonkwang.wonnature.util.ContentSanitizer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final DiscordService discordService;
    private final S3Service s3Service;

    public Long createNotice(NoticeDTO noticeDTO) {
        String sanitizedContent = ContentSanitizer.sanitize(noticeDTO.getContent()); //악성 스크립트 제거
        noticeDTO.setContent(sanitizedContent);
        Notice notice = Notice.builder()
                .title(noticeDTO.getTitle())
                .content(noticeDTO.getContent())
                .fileUrls(noticeDTO.getFileUrls())
                .build();

        Notice savedNotice = noticeRepository.save(notice);

        discordService.sendActivityMessage(noticeDTO.toString() + " : 공지를 등록했습니다.");
        log.info("{} : 공지를 등록했습니다.", noticeDTO.toString());

        return savedNotice.getId();
    }

    @Transactional
    public void updateNotice(Long noticeId, NoticeDTO noticeDTO) {
        Notice findNotice = noticeRepository.findNoticeWithUrlsById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지가 없습니다."));

        String sanitizedContent = ContentSanitizer.sanitize(noticeDTO.getContent()); //악성 스크립트 제거
        noticeDTO.setContent(sanitizedContent);

        // 기존 파일 URL 리스트
        List<String> existingImageUrls = findNotice.getFileUrls();

        // 새로운 파일 URL 리스트
        List<String> newFileUrls = noticeDTO.getFileUrls();

        // 삭제된 파일 URL 리스트
        List<String> deletedFileUrls = existingImageUrls.stream()
                .filter(url -> !newFileUrls.contains(url))
                .toList();

        // S3에서 삭제 요청
        deletedFileUrls.forEach(s3Service::deleteFile);

        findNotice.updateNotice(noticeDTO.getTitle(), sanitizedContent, newFileUrls);

        discordService.sendActivityMessage(noticeDTO.toString() + " : 공지를 수정했습니다.");
        log.info("{} : 공지 수정 완료", noticeDTO.toString());
    }

    @Transactional
    public NoticeDTO getOneNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지가 없습니다."));
        notice.addHit();

        return new NoticeDTO(notice);
    }

    public List<NoticeDTO> getNoticeList() {
        List<Notice> notices = noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
        if (notices.isEmpty()) {
            List<NoticeDTO> emptyList = new ArrayList<>();
            emptyList.add(new NoticeDTO(0L, 0L, "현재 작성된 공지가 없습니다.", "", new ArrayList<>(), LocalDateTime.now(), null));
            return emptyList;
        }

        return notices.stream().map(NoticeDTO::new).toList();

    }

    public void deleteOneNotice(Long noticeId) {
        Notice findNotice = noticeRepository.findNoticeWithUrlsById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지가 없습니다."));

        List<String> imageUrls = findNotice.getFileUrls();
        imageUrls.forEach(s3Service::deleteFile);

        noticeRepository.delete(findNotice);

        discordService.sendActivityMessage(findNotice.getTitle() + " : 공지를 삭제했습니다.");
        log.info("{} : 공지를 삭제했습니다.", findNotice.getTitle());
    }
}
