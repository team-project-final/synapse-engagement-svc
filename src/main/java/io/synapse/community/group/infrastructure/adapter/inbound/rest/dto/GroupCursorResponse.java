package io.synapse.community.group.infrastructure.adapter.inbound.rest.dto;

import java.util.List;

public record GroupCursorResponse(
        List<GroupResponse> items,
        String nextCursor,
        boolean hasNext) {
}

