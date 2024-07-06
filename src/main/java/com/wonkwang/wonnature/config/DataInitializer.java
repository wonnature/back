package com.wonkwang.wonnature.config;

import com.wonkwang.wonnature.domain.User;
import com.wonkwang.wonnature.dto.UserDTO;
import com.wonkwang.wonnature.repository.UserRepository;
import com.wonkwang.wonnature.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        List<User> all = userRepository.findAll();
        if (all.isEmpty()){
//            userService.join(new UserDTO("admin","1234")); //테스트용 데이터
        }

    }
}
