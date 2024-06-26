package com.wonkwang.wonnature.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJson;

@Service
public class DiscordService {

    @Value("${discord.error-url}")
    private String errorUrl;
    @Value("${discord.activity-url}")
    private String activityUrl;

    private void sendMessage(String url, String message) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 메시지를 1600자 단위로 나누기
        int chunkSize = 1600;
        int messageLength = message.length();
        for (int start = 0; start < messageLength; start += chunkSize) {
            int end = Math.min(messageLength, start + chunkSize);
            String chunk = message.substring(start, end);
            String jsonPayload = String.format("{\"content\": \"%s\\nTimestamp: %s\"}", escapeJson(chunk), timestamp);
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
            restTemplate.postForObject(url, request, String.class);
        }
    }

    public void sendErrorMessage(String message) {
        sendMessage(errorUrl, message);
    }

    public void sendActivityMessage(String message) {
        sendMessage(activityUrl, message);
    }
}
