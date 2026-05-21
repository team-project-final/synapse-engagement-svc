package com.synapse.engagement.community.exception;

public class InvalidGroupCursorException extends GroupException {

    public InvalidGroupCursorException() {
        super("ENGM-004", "Invalid group cursor.");
    }
}

