package io.synapse.community.group.domain.model.exception;

import java.util.UUID;

public class GroupAccessDeniedException extends GroupException {

    public GroupAccessDeniedException(UUID groupId) {
        super("ENGM-002", "Group owner permission is required: " + groupId);
    }
}

