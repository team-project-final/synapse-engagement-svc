package io.synapse.community.group.domain.model.exception;

public class InvalidGroupCursorException extends GroupException {

    public InvalidGroupCursorException() {
        super("ENGM-004", "Invalid group cursor.");
    }
}

