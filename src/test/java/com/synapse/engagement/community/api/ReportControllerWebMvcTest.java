package com.synapse.engagement.community.api;

import com.synapse.engagement.community.api.dto.ReportCreateRequest;
import com.synapse.engagement.community.api.dto.ReportModerateRequest;
import com.synapse.engagement.community.api.dto.ReportResponse;
import com.synapse.engagement.community.application.ModerationService;
import com.synapse.engagement.community.application.ReportService;
import com.synapse.engagement.community.domain.ReportStatus;
import com.synapse.engagement.community.domain.ReportTargetType;
import com.synapse.engagement.shared.ConflictException;
import com.synapse.engagement.shared.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import({GlobalExceptionHandler.class, ReportControllerWebMvcTest.AuthenticationPrincipalTestConfig.class})
class ReportControllerWebMvcTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private ModerationService moderationService;

    @Test
    void createReportUsesAuthenticatedReporterAndReturnsCreated() throws Exception {
        when(reportService.create(eq(100L), any(ReportCreateRequest.class)))
                .thenReturn(new ReportResponse(
                        1L,
                        ReportTargetType.SHARED_DECK,
                        10L,
                        "spam",
                        ReportStatus.PENDING,
                        null,
                        Instant.parse("2026-06-01T00:00:00Z"),
                        null
                ));

        mvc.perform(post("/api/v1/community/reports")
                        .with(jwt("100", "MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"SHARED_DECK","targetId":10,"reason":"spam"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.targetType").value("SHARED_DECK"))
                .andExpect(jsonPath("$.reporterId").doesNotExist());
    }

    @Test
    void duplicateReportReturnsConflict() throws Exception {
        when(reportService.create(eq(100L), any(ReportCreateRequest.class)))
                .thenThrow(new ConflictException("Report already exists for this target"));

        mvc.perform(post("/api/v1/community/reports")
                        .with(jwt("100", "MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"SHARED_NOTE","targetId":20,"reason":"duplicate"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void adminReportListRejectsNonAdminWithForbidden() throws Exception {
        mvc.perform(get("/api/v1/admin/reports")
                        .with(jwt("100", "MEMBER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminCanListAndModerateReports() throws Exception {
        when(reportService.findByStatus(ReportStatus.PENDING)).thenReturn(List.of(
                new ReportResponse(
                        1L,
                        ReportTargetType.STUDY_GROUP,
                        30L,
                        "abuse",
                        ReportStatus.PENDING,
                        null,
                        Instant.parse("2026-06-01T00:00:00Z"),
                        null
                )
        ));
        when(moderationService.moderate(eq(1L), any(ReportModerateRequest.class)))
                .thenReturn(new ReportResponse(
                        1L,
                        ReportTargetType.STUDY_GROUP,
                        30L,
                        "abuse",
                        ReportStatus.APPROVED,
                        "hidden",
                        Instant.parse("2026-06-01T00:00:00Z"),
                        Instant.parse("2026-06-01T00:10:00Z")
                ));

        mvc.perform(get("/api/v1/admin/reports")
                        .with(jwt("900", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetType").value("STUDY_GROUP"));

        mvc.perform(patch("/api/v1/admin/reports/1")
                        .with(jwt("900", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"APPROVED","adminNote":"hidden"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.adminNote").value("hidden"));
    }

    private static RequestPostProcessor jwt(String subject, String role) {
        return request -> {
            request.setAttribute("test.jwt.subject", subject);
            request.setAttribute("test.jwt.role", role);
            return request;
        };
    }

    @TestConfiguration
    static class AuthenticationPrincipalTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new TestJwtArgumentResolver());
        }
    }

    static class TestJwtArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && Jwt.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            String subject = (String) webRequest.getAttribute("test.jwt.subject", NativeWebRequest.SCOPE_REQUEST);
            String role = (String) webRequest.getAttribute("test.jwt.role", NativeWebRequest.SCOPE_REQUEST);
            if (subject == null) {
                return null;
            }
            return Jwt.withTokenValue("test-token")
                    .header("alg", "none")
                    .subject(subject)
                    .claim("roles", List.of(role))
                    .build();
        }
    }
}
