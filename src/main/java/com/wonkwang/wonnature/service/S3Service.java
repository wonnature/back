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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    private final DiscordService discordService;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private final String folderName = "tests/";

    public String uploadFile(MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();

        // 원본 파일 이름에서 확장자 추출
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String baseFilename = originalFilename.substring(0, dotIndex);

        // 원본 파일 이름을 최대 5자로 제한
        if (baseFilename.length() > 5) {
            baseFilename = baseFilename.substring(0, 5);
        }

        UUID uuid = UUID.randomUUID();
        String base64UUID = Base64.getUrlEncoder().withoutPadding().encodeToString(asBytes(uuid)); //uuid 길이 압축

        String fileName = base64UUID + "-" + baseFilename + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());

        amazonS3.putObject(new PutObjectRequest(bucketName, folderName + fileName, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        String imageUrl = amazonS3.getUrl(bucketName, folderName + fileName).toString();
        log.info("{} 이미지 업로드 완료", imageUrl);
        discordService.sendActivityMessage(imageUrl + " : 이미지 업로드 완료");
        return imageUrl;
    }

    public void deleteFile(String fileUrl)  {
        String fileName = extractFileNameFromUrl(fileUrl);
        amazonS3.deleteObject(bucketName, folderName + fileName);
        log.info("{} 이미지 삭제 완료", fileName);
        discordService.sendActivityMessage(fileUrl + " : 이미지 삭제 완료");
    }

    private String extractFileNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IllegalArgumentException("URL이 null이거나 비어 있습니다.");
        }
        String decodedUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8);
        return decodedUrl.substring(decodedUrl.lastIndexOf('/') + 1);
    }

    private byte[] asBytes(UUID uuid) { //uuid 길이 압축
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }
}
