package com.onboarding.auth.application.dtos.user;

import com.onboarding.auth.domain.user.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SignUpReqDto {
    private String username;
    private String password;
    private String nickname;
}
