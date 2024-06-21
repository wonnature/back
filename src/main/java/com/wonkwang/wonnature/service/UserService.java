package com.wonkwang.wonnature.service;

import com.wonkwang.wonnature.domain.Role;
import com.wonkwang.wonnature.domain.User;
import com.wonkwang.wonnature.dto.UserDTO;
import com.wonkwang.wonnature.dto.UserLoginResponse;
import com.wonkwang.wonnature.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DiscordService discordService;

    public UserLoginResponse login(UserDTO userDTO) {
        User findUser = userRepository.findByUsername(userDTO.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 없습니다."));

        if (findUser.getLockoutTime() != null && LocalDateTime.now().isBefore(findUser.getLockoutTime())) {
            throw new RuntimeException("계정이 잠겼습니다. 잠시 후 다시 시도해주세요."); //로그인 5번 실패 시
        }

        if (!passwordEncoder.matches(userDTO.getPassword(), findUser.getPassword())) {
            handleFailedLoginAttempt(findUser); //로그인 실패 횟수 증가
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        log.info("{} : 로그인", userDTO.getUsername());
        discordService.sendActivityMessage(userDTO.getUsername() + " : 로그인");
        resetFailedLoginAttempts(findUser); //로그인 실패 횟수 초기화

        return new UserLoginResponse(findUser.getId(), findUser.getUsername(), findUser.getRole());
    }

    private void handleFailedLoginAttempt(User user) {
        System.out.println("handle 실행");
        int newFailedAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newFailedAttempts);
        if (newFailedAttempts % 5 == 0) {
            user.setLockoutTime(LocalDateTime.now().plusMinutes(15));
            log.info("{} : 계정이 로그인 실패로 계정이 잠겼습니다.", user.getUsername());
            discordService.sendActivityMessage(user.getUsername() + " : 계정이 로그인 실패로 계정이 잠겼습니다.");
        }
        userRepository.save(user);
    }

    private void resetFailedLoginAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockoutTime(null);
        userRepository.save(user);
    }
    
    //관리자만 실행하는 메소드
    public void join(UserDTO userDTO) {
        Optional<User> findUser = userRepository.findByUsername(userDTO.getUsername());
        if (findUser.isPresent()) {
            throw new IllegalArgumentException("이미 있는 회원입니다.");
        }

        User user = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(Role.ADMIN)
                .build();

        userRepository.save(user);
    }

    public void findUserById(Long userId) {

    }
}
