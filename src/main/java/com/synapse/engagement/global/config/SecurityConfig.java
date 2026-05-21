package com.synapse.engagement.global.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    /*
     * W2는 X-User-Id 임시 헤더를 사용합니다.
     * platform JWT 연동 시 이 공통 설정 위치에 실제 SecurityFilterChain을 추가합니다.
     */
}
