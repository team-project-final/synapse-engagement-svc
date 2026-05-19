package io.synapse.community.member.exception;

import io.synapse.community.group.exception.GroupException;
import java.util.UUID;

public class MemberAlreadyExistsException extends GroupException {

    public MemberAlreadyExistsException(UUID userId) {
        super("ENGM-103", "User is already a group member: " + userId);
    }
}
