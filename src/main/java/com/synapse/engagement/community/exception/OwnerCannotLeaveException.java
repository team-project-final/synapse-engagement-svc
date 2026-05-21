package com.synapse.engagement.community.exception;

import com.synapse.engagement.community.exception.GroupException;
import java.util.UUID;

public class OwnerCannotLeaveException extends GroupException {

    public OwnerCannotLeaveException(UUID groupId) {
        super("ENGM-105", "Group owner cannot leave before transferring ownership: " + groupId);
    }
}

