package com.synapse.engagement.community.controller;

import com.synapse.engagement.community.dto.request.ShareContentRequest;
import com.synapse.engagement.community.dto.response.ShareTokenResponse;
import com.synapse.engagement.community.dto.response.SharedContentResponse;
import com.synapse.engagement.community.entity.ContentType;
import com.synapse.engagement.community.service.SharedContentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SharedContentController.class)
class SharedContentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SharedContentService sharedContentService;

    @Test
    @DisplayName("share_인증헤더없음_should401")
    void share_인증헤더없음_should401() throws Exception {
        mockMvc.perform(post("/api/v1/community/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"DECK","contentId":"00000000-0000-0000-0000-000000000001","title":"Deck"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ENGM-304"));
    }

    @Test
    @DisplayName("share_정상요청_should201")
    void share_정상요청_should201() throws Exception {
        UUID userId = UUID.randomUUID();
        given(sharedContentService.share(eq(userId), any(ShareContentRequest.class)))
                .willReturn(new ShareTokenResponse("token", "/api/v1/community/share/token"));

        mockMvc.perform(post("/api/v1/community/share")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"DECK","contentId":"00000000-0000-0000-0000-000000000001","title":"Deck"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shareToken").value("token"));
    }

    @Test
    @DisplayName("findByToken_공개조회_should200")
    void findByToken_공개조회_should200() throws Exception {
        given(sharedContentService.findByToken("token")).willReturn(response("token"));

        mockMvc.perform(get("/api/v1/community/share/token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareToken").value("token"))
                .andExpect(jsonPath("$.title").value("Deck"));
    }

    @Test
    @DisplayName("search_size범위초과_should400")
    void search_size범위초과_should400() throws Exception {
        mockMvc.perform(get("/api/v1/community/search")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ENGM-305"));
    }

    private static SharedContentResponse response(String token) {
        return new SharedContentResponse(
                UUID.randomUUID(),
                token,
                ContentType.DECK,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Deck",
                "Description",
                List.of("spring"),
                0,
                LocalDateTime.now());
    }
}

