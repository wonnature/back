package com.wonkwang.wonnature.util;

public class ContentSanitizer {

    private ContentSanitizer() {

    }

    public static String sanitize(String content) {
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
