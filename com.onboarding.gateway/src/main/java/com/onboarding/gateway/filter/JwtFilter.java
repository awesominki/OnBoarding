package com.onboarding.gateway.filter;

import com.onboarding.gateway.dto.AuthResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtFilter implements GlobalFilter, Ordered {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestPath = exchange.getRequest().getURI().getPath();

        // 회원가입 및 로그인 경로는 필터를 거치지 않도록 예외 처리
        if (requestPath.equals("/users/signup") || requestPath.equals("/users/sign")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return this.onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);  // "Bearer " 이후의 JWT 토큰 추출

        // 캐싱된 토큰과 사용자 정보를 Redis에서 조회
        Map<Object, Object> cachedTokenData = redisTemplate.opsForHash().entries(token);
        if (!cachedTokenData.isEmpty()) {
            // Redis에서 캐싱된 userId와 userRole을 가져옴
            String userId = (String) cachedTokenData.get("userId");
            String userRole = (String) cachedTokenData.get("userRole");

            // 헤더에 캐싱된 사용자 정보 추가
            return this.addUserHeaders(exchange, userId, userRole)
                    .flatMap(updatedExchange -> chain.filter(updatedExchange));
        }

        ServiceInstance authServerInstance = discoveryClient.getInstances("auth-server")
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No auth-server instances available"));

        String authServerUri = authServerInstance.getUri().toString();

        // 캐싱된 토큰이 없을 경우 auth-server로 검증 요청
        return webClientBuilder.build()
                .post()
                .uri(authServerUri+"/users/validate-token")  // auth-server로 토큰 검증 요청
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(AuthResponseDto.class)
                .flatMap(authResponse -> {
                    if (authResponse.isValid()) {
                        // 검증 성공 시 Redis에 토큰과 사용자 정보 캐싱
                        Map<String, String> tokenData = new HashMap<>();
                        tokenData.put("userId", authResponse.getUserId());
                        tokenData.put("userRole", authResponse.getUserRole());

                        redisTemplate.opsForHash().putAll(token, tokenData);

                        return this.addUserHeaders(exchange, authResponse.getUserId(), authResponse.getUserRole())
                                .flatMap(updatedExchange -> chain.filter(updatedExchange));
                    } else {
                        return this.onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                    }
                });
    }

    // 헤더에 사용자 ID와 역할 추가 메소드
    private Mono<ServerWebExchange> addUserHeaders(ServerWebExchange exchange, String userId, String userRole) {
        return Mono.fromCallable(() -> {
            ServerWebExchange updatedExchange = exchange.mutate()
                    .request(request -> request
                            .headers(headers -> {
                                headers.add("X-USER-ID", userId);
                                headers.add("X-USER-ROLE", userRole);  // 권한 정보 헤더 추가
                            })
                    ).build();
            return updatedExchange;
        });
    }

    // 에러 처리 메소드
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(err.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}

