package com.onboarding.auth.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Column(name = "nickname", nullable = false)
    private String nickName;

    public User(String userName, String encodedPassword, String nickName) {
        this.userName = userName;
        this.password = encodedPassword;
        this.userRole = UserRole.valueOf("ROLE_USER");
        this.nickName = nickName;
    }
}
