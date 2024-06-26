package com.wonkwang.wonnature.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    private final DiscordService discordService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(String folderName, MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();

        // 원본 파일 이름에서 확장자 추출
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String baseFilename = originalFilename.substring(0, dotIndex);

        // 원본 파일 이름을 최대 15자로 제한
        if (baseFilename.length() > 15) {
            baseFilename = baseFilename.substring(0, 15);
        }

        String fileName = baseFilename + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());

        amazonS3.putObject(new PutObjectRequest(bucketName, folderName + "/" + fileName, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        String imageUrl = amazonS3.getUrl(bucketName, folderName + "/" + fileName).toString();
        log.info("{} 파일 업로드 완료", imageUrl);
        discordService.sendActivityMessage(imageUrl + " : 파일 업로드 완료");
        return imageUrl;
    }

    public void deleteFile(String fileUrl) {
        String objectKey = extractObjectKeyFromUrl(fileUrl);
        amazonS3.deleteObject(bucketName, objectKey);
        log.info("{} 파일 삭제 완료", objectKey);
        discordService.sendActivityMessage(fileUrl + " : 파일 삭제 완료");
    }

    private String extractObjectKeyFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IllegalArgumentException("URL이 null이거나 비어 있습니다.");
        }
        String decodedUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8);
        // "s3.amazonaws.com/" 이후의 경로 추출
        return decodedUrl.substring(decodedUrl.indexOf(".com/") + 5); // 5는 ".com/"의 길이
    }
}
