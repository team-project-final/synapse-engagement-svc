package com.synapse.engagement.community.dto.response;

import java.util.List;

public record GroupCursorResponse(
        List<GroupResponse> items,
        String nextCursor,
        boolean hasNext) {
}

