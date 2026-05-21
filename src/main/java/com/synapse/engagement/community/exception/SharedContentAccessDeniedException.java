package com.synapse.engagement.community.exception;

import java.util.UUID;

public class SharedContentAccessDeniedException extends SharedContentException {

    public SharedContentAccessDeniedException(UUID sharedContentId) {
        super("ENGM-302", "Shared content access denied: " + sharedContentId);
    }
}

