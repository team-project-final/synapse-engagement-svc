package io.synapse.community.group.member.domain.model.exception;

import io.synapse.community.group.domain.model.exception.GroupException;
import java.util.UUID;

public class MemberRejoinBlockedException extends GroupException {

    public MemberRejoinBlockedException(UUID userId) {
        super("ENGM-104", "Kicked member cannot rejoin for 7 days: " + userId);
    }
}

