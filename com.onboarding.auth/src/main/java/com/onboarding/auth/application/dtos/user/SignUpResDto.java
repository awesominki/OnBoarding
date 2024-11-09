package com.onboarding.auth.application.dtos.user;

import com.onboarding.auth.domain.user.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpResDto {
    private String username;
    private String nickname;
    private UserRole authorities;

    public SignUpResDto(String username, String nickname, UserRole authorities) {
        this.username = username;
        this.nickname = nickname;
        this.authorities = authorities;
    }
}
