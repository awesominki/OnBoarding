package com.onboarding.auth.application.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignInReqDto {
    private String username;
    private String password;
}
