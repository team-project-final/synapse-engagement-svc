package io.synapse.community.group.dto;

import java.util.List;

public record GroupCursorResponse(
        List<GroupResponse> items,
        String nextCursor,
        boolean hasNext) {
}
