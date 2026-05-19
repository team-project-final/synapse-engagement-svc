package io.synapse.community.member.exception;

import io.synapse.community.group.exception.GroupException;
import java.util.UUID;

public class MemberPermissionDeniedException extends GroupException {

    public MemberPermissionDeniedException(UUID groupId) {
        super("ENGM-102", "Group member permission is required: " + groupId);
    }
}
