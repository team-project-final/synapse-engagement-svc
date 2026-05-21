package com.synapse.engagement.community.exception;

import com.synapse.engagement.community.exception.GroupException;
import java.util.UUID;

public class GroupMemberPermissionDeniedException extends GroupException {

    public GroupMemberPermissionDeniedException(UUID groupId) {
        super("ENGM-102", "Group member permission is required: " + groupId);
    }
}

