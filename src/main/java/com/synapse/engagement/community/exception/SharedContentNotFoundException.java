package com.synapse.engagement.community.exception;

public class SharedContentNotFoundException extends SharedContentException {

    public SharedContentNotFoundException(String identifier) {
        super("ENGM-301", "Shared content not found: " + identifier);
    }
}

