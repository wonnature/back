package com.wonkwang.wonnature.service;

import com.wonkwang.wonnature.domain.Notice;
import com.wonkwang.wonnature.dto.NoticeDTO;
import com.wonkwang.wonnature.repository.NoticeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final S3Service s3Service;

    public Long createNotice(NoticeDTO noticeDTO) {
        String sanitizedContent = sanitize(noticeDTO.getContent()); //악성 스크립트 제거
        noticeDTO.setContent(sanitizedContent);
        Notice notice = Notice.builder()
                .title(noticeDTO.getTitle())
                .content(noticeDTO.getContent())
                .fileUrls(noticeDTO.getFileUrls())
                .build();

        Notice savedNotice = noticeRepository.save(notice);

        return savedNotice.getId();
    }

    @Transactional
    public void updateNotice(Long noticeId, NoticeDTO noticeDTO) {
        Notice findNotice = noticeRepository.findNoticeWithUrlsById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지가 없습니다."));

        String sanitizedContent = sanitize(noticeDTO.getContent()); //악성 스크립트 제거
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
    }

    @Transactional
    public NoticeDTO getOneNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지가 없습니다."));
        notice.addHit();

        return new NoticeDTO(notice);
    }

    public List<NoticeDTO> getNoticeList() {
        List<Notice> notices = noticeRepository.findAll();

        return notices.stream().map(NoticeDTO::new).toList();

    }

    public void deleteOneNotice(Long noticeId) {
        Notice findNotice = noticeRepository.findNoticeWithUrlsById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지가 없습니다."));

        List<String> imageUrls = findNotice.getFileUrls();
        imageUrls.forEach(s3Service::deleteFile);

        noticeRepository.delete(findNotice);
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
