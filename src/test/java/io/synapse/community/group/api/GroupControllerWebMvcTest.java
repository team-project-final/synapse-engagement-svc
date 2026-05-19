package io.synapse.community.group.api;

import io.synapse.community.group.service.GroupService;
import io.synapse.community.group.dto.GroupCursorResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
class GroupControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupService groupService;

    @Test
    @DisplayName("listGroups_인증헤더없음_should401")
    void listGroups_인증헤더없음_should401() throws Exception {
        mockMvc.perform(get("/api/v1/groups"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ENGM-005"));
    }

    @Test
    @DisplayName("listGroups_정상요청_should200")
    void listGroups_정상요청_should200() throws Exception {
        given(groupService.listGroups(isNull(), org.mockito.ArgumentMatchers.eq(20)))
                .willReturn(new GroupCursorResponse(List.of(), null, false));

        mockMvc.perform(get("/api/v1/groups")
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.hasNext").value(false));
    }
}
