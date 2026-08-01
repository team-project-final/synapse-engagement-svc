package com.synapse.engagement.community;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.engagement.support.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupSharedContentE2ETests {

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void groupScopedShareIsVisibleToMembersOnlyAndHiddenFromPublicSearch() throws Exception {
        var ownerToken = bearer("52100");
        var outsiderToken = bearer("52101");

        var groupId = json(mvc.perform(post("/api/v1/community/groups")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"FE05 Study Group","description":"group scoped share","isPublic":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asLong();

        // 1) 그룹 멤버(= 소유자)는 그룹 공유를 만들 수 있다.
        var shareToken = json(mvc.perform(post("/api/v1/community/share")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"DECK","contentId":52001,"title":"FE05 Group Deck","description":"members only","tags":["fe05"],"groupId":%d}
                                """.formatted(groupId)))
                .andExpect(status().isCreated())
                .andReturn()).get("shareToken").asText();

        // 2) 멤버는 그룹 목록에서 볼 수 있다.
        mvc.perform(get("/api/v1/community/groups/" + groupId + "/shared-content")
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("FE05 Group Deck"))
                .andExpect(jsonPath("$[0].groupId").value(groupId));

        // 3) 비멤버는 그룹 목록에 접근할 수 없다(403).
        mvc.perform(get("/api/v1/community/groups/" + groupId + "/shared-content")
                        .header("Authorization", outsiderToken))
                .andExpect(status().isForbidden());

        // 4) 없는 그룹은 404다.
        mvc.perform(get("/api/v1/community/groups/99999999/shared-content")
                        .header("Authorization", ownerToken))
                .andExpect(status().isNotFound());

        // 5) 익명 토큰 조회는 존재를 감춘다(404).
        mvc.perform(get("/api/v1/community/share/" + shareToken))
                .andExpect(status().isNotFound());

        // 6) 비멤버 토큰 조회도 404다.
        mvc.perform(get("/api/v1/community/share/" + shareToken)
                        .header("Authorization", outsiderToken))
                .andExpect(status().isNotFound());

        // 7) 멤버는 토큰으로 조회할 수 있다.
        mvc.perform(get("/api/v1/community/share/" + shareToken)
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId));

        // 8) 공개 검색에는 그룹 공유가 노출되지 않는다.
        var publicSearch = mvc.perform(get("/api/v1/community/search").param("q", "FE05 Group Deck"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(publicSearch).doesNotContain("FE05 Group Deck");
    }

    @Test
    void nonMemberCannotShareToGroup() throws Exception {
        var ownerToken = bearer("52200");
        var outsiderToken = bearer("52201");

        var groupId = json(mvc.perform(post("/api/v1/community/groups")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"FE05 Closed Group","description":"no outsiders","isPublic":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn()).get("id").asLong();

        mvc.perform(post("/api/v1/community/share")
                        .header("Authorization", outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"NOTE","contentId":52002,"title":"Outsider Note","tags":[],"groupId":%d}
                                """.formatted(groupId)))
                .andExpect(status().isForbidden());
    }

    private JsonNode json(org.springframework.test.web.servlet.MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String bearer(String subject) {
        return "Bearer " + TestJwt.accessToken(subject);
    }
}
