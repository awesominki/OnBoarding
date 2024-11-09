package com.onboarding.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:19092") // 모든 Origin 허용
                .allowedMethods("*") // 모든 메서드 허용
                .allowedHeaders("*") // 모든 Header 허용
                .allowCredentials(true); // 자격 증명 허용
    }
}
