package com.onboarding.auth.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String BEARER_PREFIX = "Bearer ";

    // JWT 토큰 생성
    public String generateToken(Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("X-USER-ID", userId);
        claims.put("X-USER-ROLE", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())  // userId를 subject로 설정
                .setIssuedAt(new Date())  // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))  // 만료 시간
                .signWith(SignatureAlgorithm.HS512, jwtSecret)  // 서명 알고리즘과 비밀키 설정
                .compact();
    }

    // header 에서 JWT 가져오기
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); // 키 값으로 가져오기
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecret));
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token).getBody();
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // 토큰에서 클레임(Claims)을 추출하는 메소드
    public Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecret));
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token).getBody();

        return claims;

    }

    // 토큰에서 User ID 추출
    public String getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("X-USER-ID", String.class);  // 클레임에서 'X-USER-ID' 추출
    }

    // 토큰에서 User Role 추출
    public String getUserRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("X-USER-ROLE", String.class);  // 클레임에서 'X-USER-ROLE' 추출
    }
}
