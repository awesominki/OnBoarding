package com.onboarding.auth.application.dtos.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInResDto {
    private String token;

    public SignInResDto(String token) {
        this.token = token;
    }
}
