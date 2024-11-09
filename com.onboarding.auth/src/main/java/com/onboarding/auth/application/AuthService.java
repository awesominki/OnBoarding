package com.onboarding.auth.application;

import com.onboarding.auth.application.dtos.user.SignUpReqDto;
import com.onboarding.auth.application.dtos.user.SignUpResDto;
import com.onboarding.auth.config.JwtTokenProvider;
import com.onboarding.auth.domain.user.User;
import com.onboarding.auth.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    // 회원가입 로직
    public SignUpResDto registerUser(SignUpReqDto signUpReqDto) {
        // 사용자 이름 중복 확인
        if (userRepository.existsByUserName(signUpReqDto.getUsername())) {
            throw new IllegalArgumentException("이미 가입된 사용자 입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signUpReqDto.getPassword());

        // 새로운 사용자 저장
        User newUser = new User(signUpReqDto.getUsername(),
                encodedPassword,
                signUpReqDto.getNickname()
        );

        User savedUser = userRepository.save(newUser);

        return new SignUpResDto(savedUser.getUserName(), savedUser.getNickName(), savedUser.getUserRole());
    }

    // 로그인 로직
    public String login(String username, String password) {
        // 사용자 조회
        Optional<User> userOptional = userRepository.findByUserName(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("해당 아이디는 가입되지 않았습니다.");
        }

        User user = userOptional.get();

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("틀린 비밀번호 입니다.");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUserRole().name());

        // 사용자 정보와 함께 Redis에 저장
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("userId", String.valueOf(user.getId()));
        tokenData.put("userRole", user.getUserRole().name());

        // Redis에 토큰과 사용자 정보를 캐싱 (해시 형태로 저장)
        redisTemplate.opsForHash().putAll(token, tokenData);

        // JWT 토큰 생성
        return token;
    }
}
