package com.synapse.engagement.community.exception;

import com.synapse.engagement.community.exception.GroupException;
import java.util.UUID;

public class GroupMemberNotFoundException extends GroupException {

    public GroupMemberNotFoundException(UUID memberId) {
        super("ENGM-101", "Group member not found: " + memberId);
    }
}

