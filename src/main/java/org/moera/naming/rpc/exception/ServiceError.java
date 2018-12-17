package org.moera.naming.rpc.exception;

public enum ServiceError {

    NAME_EMPTY(1, "name is empty");

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
