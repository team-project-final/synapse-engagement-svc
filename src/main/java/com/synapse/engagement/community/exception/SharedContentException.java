package com.synapse.engagement.community.exception;

public abstract class SharedContentException extends RuntimeException {

    private final String code;

    protected SharedContentException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}

