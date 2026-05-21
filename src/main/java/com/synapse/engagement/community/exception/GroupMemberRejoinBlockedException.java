package com.synapse.engagement.community.exception;

import com.synapse.engagement.community.exception.GroupException;
import java.util.UUID;

public class GroupMemberRejoinBlockedException extends GroupException {

    public GroupMemberRejoinBlockedException(UUID userId) {
        super("ENGM-104", "Kicked member cannot rejoin for 7 days: " + userId);
    }
}

