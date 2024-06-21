package com.wonkwang.wonnature.exception;


import com.wonkwang.wonnature.dto.ResponseDTO;
import com.wonkwang.wonnature.service.DiscordService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.wonkwang.wonnature.dto.ResponseEntityBuilder.build;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final DiscordService discordService;

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseDTO<?>> handleRuntimeException(RuntimeException ex) {
        String exceptionClassName = ex.getClass().getName();
        String message = String.format("handleRuntimeException : %s : %s ,%s", exceptionClassName, ex.getMessage(), ex);
        log.error(message);
        discordService.sendErrorMessage("RuntimeException: " + message);
        return build(ex.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpSessionRequiredException.class)
    public ResponseEntity<ResponseDTO<?>> handleSessionRequiredException(HttpSessionRequiredException ex, HttpServletResponse response) {
        String exceptionClassName = ex.getClass().getName();
        String message = String.format("handleSessionRequiredException : %s : %s", exceptionClassName, ex.getMessage());
        log.error(message);
        discordService.sendErrorMessage("HttpSessionRequiredException: " + message);
        deleteClientCookie(response);
        return build("로그인 세션이 만료되었거나 없습니다.", UNAUTHORIZED);
    }

    @ExceptionHandler(ServletRequestBindingException.class) // 주로 이 예외가 발생함
    public ResponseEntity<ResponseDTO<?>> handleSessionExpired(ServletRequestBindingException ex, HttpServletResponse response) {
        String exceptionClassName = ex.getClass().getName();
        String message = String.format("handleSessionExpired : %s : %s", exceptionClassName, ex.getMessage());
        log.error(message);
        discordService.sendErrorMessage("ServletRequestBindingException: " + message);
        deleteClientCookie(response);
        return build("로그인 세션이 만료되었거나 없습니다.", UNAUTHORIZED);
    }

    private static void deleteClientCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0); // 쿠키 만료 시간 설정 (0으로 설정하여 즉시 삭제)
        response.addCookie(cookie);
    }
}
