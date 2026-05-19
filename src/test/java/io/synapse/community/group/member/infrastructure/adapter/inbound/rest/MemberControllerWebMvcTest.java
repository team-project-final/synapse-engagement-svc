package io.synapse.community.group.member.infrastructure.adapter.inbound.rest;

import io.synapse.community.group.member.application.usecase.MemberService;
import io.synapse.community.group.member.infrastructure.adapter.inbound.rest.dto.MemberResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("list_인증헤더없음_should401")
    void list_인증헤더없음_should401() throws Exception {
        mockMvc.perform(get("/api/v1/groups/{groupId}/members", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("list_정상요청_should200")
    void list_정상요청_should200() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        given(memberService.list(userId, groupId)).willReturn(List.<MemberResponse>of());

        mockMvc.perform(get("/api/v1/groups/{groupId}/members", groupId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());
    }
}

