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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommunityStep11E2ETests {
    @Autowired
    private MockMvc mvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shareSearchForkReportAndModerationFlowWorksEndToEnd() throws Exception {
        var ownerToken = bearer("11100");
        var forkOwnerToken = bearer("11101");
        var reporterToken = bearer("11102");
        var adminToken = bearer("11900", List.of("ADMIN"));

        var original = json(mvc.perform(post("/api/v1/community/share")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"DECK","contentId":91001,"title":"Step11 Deck Alpha","description":"Step11 searchable deck","tags":["step11","deck"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shareToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString());
        var originalToken = original.get("shareToken").asText();

        var originalDetail = json(mvc.perform(get("/api/v1/community/share/" + originalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(11100))
                .andExpect(jsonPath("$.title").value("Step11 Deck Alpha"))
                .andExpect(jsonPath("$.tags[*]", hasItem("step11")))
                .andReturn().getResponse().getContentAsString());
        var originalId = originalDetail.get("id").asLong();

        mvc.perform(get("/api/v1/community/search?q=Step11&contentType=DECK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Step11 Deck Alpha")));

        var forked = json(mvc.perform(post("/api/v1/community/share/" + originalToken + "/fork")
                        .header("Authorization", forkOwnerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(11101))
                .andExpect(jsonPath("$.sourceShareId").isNumber())
                .andReturn().getResponse().getContentAsString());
        var forkedId = forked.get("id").asLong();
        var forkedToken = forked.get("shareToken").asText();

        mvc.perform(get("/api/v1/community/share/" + originalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadCount").value(1));

        mvc.perform(get("/api/v1/community/share/" + forkedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(11101))
                .andExpect(jsonPath("$.sourceShareId").value(originalId));

        mvc.perform(post("/api/v1/community/reports")
                        .header("Authorization", reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"SHARED_DECK","targetId":%d,"reason":"Step11 moderation target"}
                                """.formatted(forkedId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.targetType").value("SHARED_DECK"))
                .andExpect(jsonPath("$.targetId").value(forkedId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.reporterId").doesNotExist());

        mvc.perform(post("/api/v1/community/reports")
                        .header("Authorization", reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"SHARED_DECK","targetId":%d,"reason":"duplicate"}
                                """.formatted(forkedId)))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/v1/admin/reports")
                        .header("Authorization", reporterToken))
                .andExpect(status().isForbidden());

        var pendingReports = json(mvc.perform(get("/api/v1/admin/reports?status=PENDING")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].targetId", hasItem((int) forkedId)))
                .andReturn().getResponse().getContentAsString());
        var reportId = findReportIdByTargetId(pendingReports, forkedId);

        mvc.perform(patch("/api/v1/admin/reports/" + reportId)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"APPROVED","adminNote":"Step11 hidden by moderation"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.adminNote").value("Step11 hidden by moderation"));

        mvc.perform(get("/api/v1/community/share/" + forkedToken))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/community/share/" + originalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(originalId))
                .andExpect(jsonPath("$.title").value("Step11 Deck Alpha"));
    }

    private JsonNode json(String body) throws Exception {
        return objectMapper.readTree(body);
    }

    private long findReportIdByTargetId(JsonNode reports, long targetId) {
        for (JsonNode report : reports) {
            if (report.get("targetId").asLong() == targetId) {
                return report.get("id").asLong();
            }
        }
        throw new AssertionError("Pending report not found for targetId=" + targetId);
    }

    private String bearer(String subject) {
        return "Bearer " + TestJwt.accessToken(subject);
    }

    private String bearer(String subject, List<String> roles) {
        return "Bearer " + TestJwt.accessToken(subject, roles);
    }
}
