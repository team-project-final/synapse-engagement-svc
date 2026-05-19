package io.synapse.community.group.domain.model.exception;

import java.util.UUID;

public class GroupNotFoundException extends GroupException {

    public GroupNotFoundException(UUID groupId) {
        super("ENGM-001", "Group not found: " + groupId);
    }
}

