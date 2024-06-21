package com.wonkwang.wonnature.controller;


import com.wonkwang.wonnature.domain.User;
import com.wonkwang.wonnature.dto.ResponseDTO;
import com.wonkwang.wonnature.dto.UserDTO;
import com.wonkwang.wonnature.dto.UserLoginResponse;
import com.wonkwang.wonnature.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.wonkwang.wonnature.dto.ResponseEntityBuilder.build;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<UserLoginResponse>> login(@RequestBody UserDTO userDTO, HttpServletRequest request) {

        UserLoginResponse userLoginResponse = userService.login(userDTO);

        request.getSession().invalidate(); //기존 세션 파기
        HttpSession session = request.getSession(true); //세션이 없으면 생성
        session.setAttribute("userId", userLoginResponse.getId());
        session.setAttribute("role", userLoginResponse.getRole());
        session.setMaxInactiveInterval(3600); //세션 유지 시간 (초)

        log.info("로그인 성공 {}",userLoginResponse.getUsername());

        return build("로그인을 성공했습니다.", OK, userLoginResponse);
    }

    @GetMapping("/logout")
    public ResponseEntity<ResponseDTO<?>> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);//세션이 없으면 null return

        if (session != null) {
            session.invalidate();
            deleteClientCookie(response);
            return build("로그아웃을 성공했습니다.", OK);
        } else {
            return build("현재 로그인 상태가 아닙니다.", BAD_REQUEST);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<ResponseDTO<UserLoginResponse>> checkState(HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        @SessionAttribute(required = false) Long userId) {
        // JSESSIONID 쿠키 확인
        boolean sessionIdExists = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    sessionIdExists = true;
                    break;
                }
            }
        }
        if (!sessionIdExists) {
            return build("애초에 세션이 존재하지 않음", BAD_REQUEST, null);
        }

        // 세션이 만료되었는지 확인
        HttpSession session = request.getSession(false);
        if (session == null || userId == null) {

            deleteClientCookie(response);
            return build("로그인 세션이 만료되었습니다.", HttpStatus.UNAUTHORIZED, null);
        }

        UserLoginResponse userLoginResponse = userService.userCheck(userId);

        // 세션이 유효한 경우
        return build("검증성공", HttpStatus.OK, userLoginResponse);
    }


    @GetMapping("/test")
    public ResponseEntity<ResponseDTO<Long>> test(@SessionAttribute("userId") Long userId) {


        return build("검증성공", OK, userId);
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
