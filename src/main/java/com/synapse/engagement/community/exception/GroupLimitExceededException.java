package com.synapse.engagement.community.exception;

public class GroupLimitExceededException extends GroupException {

    public GroupLimitExceededException(int limit) {
        super("ENGM-003", "A user can create up to " + limit + " groups.");
    }
}

