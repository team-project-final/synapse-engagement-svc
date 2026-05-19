package io.synapse.community.group.member.domain.model.exception;

import io.synapse.community.group.domain.model.exception.GroupException;
import java.util.UUID;

public class OwnerCannotLeaveException extends GroupException {

    public OwnerCannotLeaveException(UUID groupId) {
        super("ENGM-105", "Group owner cannot leave before transferring ownership: " + groupId);
    }
}

