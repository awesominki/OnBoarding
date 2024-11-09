package com.onboarding.auth.controller;

import com.onboarding.auth.application.AuthService;
import com.onboarding.auth.application.dtos.auth.AuthResponseDto;
import com.onboarding.auth.application.dtos.user.SignInReqDto;
import com.onboarding.auth.application.dtos.user.SignInResDto;
import com.onboarding.auth.application.dtos.user.SignUpReqDto;
import com.onboarding.auth.application.dtos.user.SignUpResDto;
import com.onboarding.auth.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<SignUpResDto> registerUser(@RequestBody SignUpReqDto signUpReqDto) {
        return ResponseEntity.ok(authService.registerUser(signUpReqDto));
    }

    // 로그인 API
    @PostMapping("/sign")
    public ResponseEntity<SignInResDto> login(@RequestBody SignInReqDto signInReqDto) {
        String token = authService.login(signInReqDto.getUsername(), signInReqDto.getPassword());
        return ResponseEntity.ok(new SignInResDto(token));  // JWT 토큰 반환
    }

    @PostMapping("/validate-token")
    public AuthResponseDto validateToken(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.substring(7);  // "Bearer " 부분 제거

        boolean isValid = jwtTokenProvider.validateToken(token);
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        String userRole = jwtTokenProvider.getUserRoleFromToken(token);

        // 검증 결과와 함께 AuthResponse 반환
        return new AuthResponseDto(isValid, userId, userRole);
    }


}