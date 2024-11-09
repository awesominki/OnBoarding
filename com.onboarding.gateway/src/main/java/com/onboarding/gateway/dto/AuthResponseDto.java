package com.onboarding.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private boolean valid;
    private String userId;
    private String userRole;
}
