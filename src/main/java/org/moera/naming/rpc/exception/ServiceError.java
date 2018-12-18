package org.moera.naming.rpc.exception;

public enum ServiceError {

    NAME_EMPTY(1, "name is empty"),
    NAME_TOO_LONG(2, "name is too long"),
    NAME_FORBIDDEN_CHARS(3, "name contains forbidden characters");

    private int code;
    private String message;

    ServiceError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
