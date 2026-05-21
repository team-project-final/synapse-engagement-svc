package com.synapse.engagement.community.exception;

import com.synapse.engagement.community.exception.GroupException;
import java.util.UUID;

public class GroupMemberAlreadyExistsException extends GroupException {

    public GroupMemberAlreadyExistsException(UUID userId) {
        super("ENGM-103", "User is already a group member: " + userId);
    }
}

