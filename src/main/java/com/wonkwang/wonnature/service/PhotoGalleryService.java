package com.wonkwang.wonnature.service;

import com.wonkwang.wonnature.domain.PhotoGallery;
import com.wonkwang.wonnature.dto.PhotoGalleryDTO;
import com.wonkwang.wonnature.repository.PhotoGalleryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoGalleryService {

    private final PhotoGalleryRepository photoGalleryRepository;
    private final DiscordService discordService;
    private final S3Service s3Service;

    public Long createPhotoGallery(PhotoGalleryDTO photoGalleryDTO) {
        String sanitizedContent = sanitize(photoGalleryDTO.getContent()); //악성 스크립트 제거
        photoGalleryDTO.setContent(sanitizedContent);
        PhotoGallery photoGallery = PhotoGallery.builder()
                .title(photoGalleryDTO.getTitle())
                .content(photoGalleryDTO.getContent())
                .imageUrls(photoGalleryDTO.getImageUrls())
                .build();

        PhotoGallery savedPhotoGallery = photoGalleryRepository.save(photoGallery);

        discordService.sendActivityMessage(photoGalleryDTO.toString() + " : 포토 갤러리를 등록했습니다.");
        log.info("{} : 포토 갤러리를 등록했습니다.", photoGalleryDTO.toString());

        return savedPhotoGallery.getId();
    }

    @Transactional
    public void updatePhotoGallery(Long photoGalleryId, PhotoGalleryDTO photoGalleryDTO) {
        PhotoGallery findPhotoGallery = photoGalleryRepository.findPhotoGalleryWithUrlsById(photoGalleryId)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지가 없습니다."));

        String sanitizedContent = sanitize(photoGalleryDTO.getContent()); //악성 스크립트 제거
        photoGalleryDTO.setContent(sanitizedContent);

        // 기존 파일 URL 리스트
        List<String> existingImageUrls = findPhotoGallery.getImageUrls();

        // 새로운 파일 URL 리스트
        List<String> newFileUrls = photoGalleryDTO.getImageUrls();

        // 삭제된 파일 URL 리스트
        List<String> deletedFileUrls = existingImageUrls.stream()
                .filter(url -> !newFileUrls.contains(url))
                .toList();

        // S3에서 삭제 요청
        deletedFileUrls.forEach(s3Service::deleteFile);

        findPhotoGallery.updatePhotoGallery(photoGalleryDTO.getTitle(), sanitizedContent, newFileUrls);

        discordService.sendActivityMessage(photoGalleryDTO.toString() + " : 포토 갤러리를 수정했습니다.");
        log.info("{} : 포토 갤러리를 수정했습니다.", photoGalleryDTO.toString());
    }

    @Transactional
    public PhotoGalleryDTO getOnePhotoGallery(Long photoGalleryId) {
        PhotoGallery photoGallery = photoGalleryRepository.findById(photoGalleryId)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지가 없습니다."));
        photoGallery.addHit();

        return new PhotoGalleryDTO(photoGallery);
    }

    public List<PhotoGalleryDTO> getPhotoGalleryList() {
        List<PhotoGallery> photoGallerys = photoGalleryRepository.findAll();

        return photoGallerys.stream().map(PhotoGalleryDTO::new).toList();

    }

    public void deleteOnePhotoGallery(Long photoGalleryId) {
        PhotoGallery findPhotoGallery = photoGalleryRepository.findPhotoGalleryWithUrlsById(photoGalleryId)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지가 없습니다."));

        List<String> imageUrls = findPhotoGallery.getImageUrls();
        imageUrls.forEach(s3Service::deleteFile);

        photoGalleryRepository.delete(findPhotoGallery);

        discordService.sendActivityMessage(findPhotoGallery.getTitle() + " : 포토 갤러리를 삭제했습니다.");
        log.info("{} : 포토 갤러리를 삭제했습니다.", findPhotoGallery.getTitle());
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