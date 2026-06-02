package com.synapse.engagement.community.api;

import com.synapse.engagement.community.api.dto.InviteDecisionResponse;
import com.synapse.engagement.community.api.dto.JoinRequestResponse;
import com.synapse.engagement.community.api.dto.MemberResponse;
import com.synapse.engagement.community.application.MemberService;
import com.synapse.engagement.community.domain.MemberRole;
import com.synapse.engagement.community.domain.MemberStatus;
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

@WebMvcTest(MemberController.class)
@Import({GlobalExceptionHandler.class, MemberControllerWebMvcTest.AuthenticationPrincipalTestConfig.class})
class MemberControllerWebMvcTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    void inviteAcceptAndDeclineUseAuthenticatedSubject() throws Exception {
        var expiresAt = Instant.parse("2026-06-04T00:00:00Z");
        when(memberService.invite(eq(10L), eq(100L), any()))
                .thenReturn(new InviteDecisionResponse(10L, 200L, MemberStatus.INVITED, "token-1", expiresAt));
        when(memberService.acceptInvite(10L, 200L, "token-1"))
                .thenReturn(new InviteDecisionResponse(10L, 200L, MemberStatus.ACTIVE, null, null));
        when(memberService.declineInvite(10L, 200L, "token-2"))
                .thenReturn(new InviteDecisionResponse(10L, 200L, MemberStatus.DECLINED, null, null));

        mvc.perform(post("/api/v1/community/groups/10/members/invite")
                        .with(jwtSubject("100"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":200}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("INVITED"))
                .andExpect(jsonPath("$.inviteToken").value("token-1"));

        mvc.perform(post("/api/v1/community/groups/10/invite/token-1/accept")
                        .with(jwtSubject("200")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mvc.perform(post("/api/v1/community/groups/10/invite/token-2/decline")
                        .with(jwtSubject("200")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"));
    }

    @Test
    void joinRequestsCanBeListedAndDecided() throws Exception {
        when(memberService.listJoinRequests(10L, 100L)).thenReturn(List.of(
                new JoinRequestResponse(1L, 10L, 300L, MemberRole.MEMBER, MemberStatus.PENDING, null)
        ));
        when(memberService.decideJoinRequest(eq(10L), eq(100L), eq(300L), any()))
                .thenReturn(new MemberResponse(1L, 10L, 300L, MemberRole.MEMBER, MemberStatus.ACTIVE, Instant.parse("2026-05-28T00:00:00Z")));

        mvc.perform(get("/api/v1/community/groups/10/join-requests")
                        .with(jwtSubject("100")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(300))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        mvc.perform(patch("/api/v1/community/groups/10/join-requests/300")
                        .with(jwtSubject("100"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"decision":"APPROVE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void protectedStep7EndpointsRejectMissingJwt() throws Exception {
        mvc.perform(get("/api/v1/community/groups/10/join-requests"))
                .andExpect(status().isUnauthorized());
    }

    private static RequestPostProcessor jwtSubject(String subject) {
        return request -> {
            request.setAttribute("test.jwt.subject", subject);
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
            if (subject == null) {
                return null;
            }
            return Jwt.withTokenValue("test-token")
                    .header("alg", "none")
                    .subject(subject)
                    .build();
        }
    }
}
