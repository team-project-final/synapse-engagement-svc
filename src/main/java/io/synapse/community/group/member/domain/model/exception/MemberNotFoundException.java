package io.synapse.community.group.member.domain.model.exception;

import io.synapse.community.group.domain.model.exception.GroupException;
import java.util.UUID;

public class MemberNotFoundException extends GroupException {

    public MemberNotFoundException(UUID memberId) {
        super("ENGM-101", "Group member not found: " + memberId);
    }
}

