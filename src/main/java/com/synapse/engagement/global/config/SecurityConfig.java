package com.synapse.engagement.global.config;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * platform-svc가 RS256으로 발급한 JWT를 검증하는 Resource Server 설정.
 * 공개키는 synapse.jwt.public-key(raw base64 X509 또는 PEM)로 주입한다.
 */
@Configuration
public class SecurityConfig {

    @Bean
    @Order(0)
    SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher(SecurityConfig::isPublicActuatorRequest)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }

    @Bean
    @Order(1)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // REST API 서버라서 브라우저 세션/폼 기반 CSRF 보호는 사용하지 않는다.
            // 인증은 매 요청의 Authorization: Bearer <JWT> 헤더로 처리한다.
            .csrf(AbstractHttpConfigurer::disable)

            // JWT 방식은 서버가 로그인 세션을 저장하지 않는 stateless 구조다.
            // 같은 사용자인지는 세션이 아니라 매 요청의 JWT 서명과 subject로 판단한다.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 운영/문서 확인용 엔드포인트는 인증 없이 열어둔다.
                .requestMatchers(EndpointRequest.to("health", "info", "prometheus")).permitAll()
                .requestMatchers(SecurityConfig::isPublicActuatorRequest).permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                // 공유 토큰 조회와 커뮤니티 검색은 공개 기능이므로 GET만 인증 없이 허용한다.
                .requestMatchers(HttpMethod.GET, "/api/v1/community/share/*", "/api/v1/community/search").permitAll()

                // 리더보드는 사용자별 민감 데이터를 수정하지 않는 공개 조회 API로 둔다.
                .requestMatchers(HttpMethod.GET, "/api/v1/gamification/leaderboard").permitAll()

                // 위에서 명시한 공개 API를 제외한 모든 요청은 JWT 인증이 필요하다.
                .anyRequest().authenticated())

            // Spring Security Resource Server가 Bearer token을 꺼내 JwtDecoder로 서명을 검증한다.
            // 검증에 성공하면 컨트롤러의 @AuthenticationPrincipal Jwt 파라미터로 주입된다.
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
            .build();
    }

    private static boolean isPublicActuatorRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path.equals("/actuator/health") || path.equals("/actuator/info") || path.equals("/actuator/prometheus");
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${synapse.jwt.public-key:}") String publicKey) {
        // platform-svc가 RS256 개인키로 서명한 JWT를 engagement-svc는 공개키로 검증한다.
        // dev/test에서 공개키가 비어 있으면 앱 구동만 가능하도록 임시 공개키를 생성한다.

        RSAPublicKey key = StringUtils.hasText(publicKey) ? parsePublicKey(publicKey) : generateEphemeralPublicKey();
        return NimbusJwtDecoder.withPublicKey(key).build();
    }

    private RSAPublicKey parsePublicKey(String publicKey) {
        try {
            // 설정값은 PEM 형식과 raw base64 X509 형식을 모두 받을 수 있게 정규화한다.

            String normalized = publicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(normalized);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception ex) {
            throw new IllegalStateException("synapse.jwt.public-key 파싱에 실패했습니다", ex);
        }
    }

    private RSAPublicKey generateEphemeralPublicKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            return (RSAPublicKey) keyPair.getPublic();
        } catch (Exception ex) {
            throw new IllegalStateException("임시 JWT 공개키 생성에 실패했습니다", ex);
        }
    }
}
