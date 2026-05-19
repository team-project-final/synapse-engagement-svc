package io.synapse.community.member.exception;

import io.synapse.community.group.exception.GroupException;
import java.util.UUID;

public class MemberNotFoundException extends GroupException {

    public MemberNotFoundException(UUID memberId) {
        super("ENGM-101", "Group member not found: " + memberId);
    }
}
