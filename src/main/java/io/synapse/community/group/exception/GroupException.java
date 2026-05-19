package io.synapse.community.group.exception;

public abstract class GroupException extends RuntimeException {

    private final String code;

    protected GroupException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
