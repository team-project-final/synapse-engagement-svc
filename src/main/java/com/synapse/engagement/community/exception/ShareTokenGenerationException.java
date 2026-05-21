package com.synapse.engagement.community.exception;

public class ShareTokenGenerationException extends SharedContentException {

    public ShareTokenGenerationException() {
        super("ENGM-303", "Failed to generate unique share token.");
    }
}

