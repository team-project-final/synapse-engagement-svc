package io.synapse.community.group.member.domain.model.exception;

import io.synapse.community.group.domain.model.exception.GroupException;
import java.util.UUID;

public class MemberPermissionDeniedException extends GroupException {

    public MemberPermissionDeniedException(UUID groupId) {
        super("ENGM-102", "Group member permission is required: " + groupId);
    }
}

