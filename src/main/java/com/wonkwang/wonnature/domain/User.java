package com.wonkwang.wonnature.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;

    @Setter
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int failedLoginAttempts;
    @Setter
    private LocalDateTime lockoutTime;

    @Enumerated(EnumType.STRING)
    private Role role;


    @Builder
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}

