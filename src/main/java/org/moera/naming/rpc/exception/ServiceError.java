package org.moera.naming.rpc.exception;

public enum ServiceError {

    NAME_EMPTY(1, "name is empty"),
    NAME_TOO_LONG(2, "name is too long"),
    NAME_FORBIDDEN_CHARS(3, "name contains forbidden characters"),
    NODE_URI_TOO_LONG(4, "nodeUri is too long"),
    UPDATING_KEY_INVALID_ENCODING(5, "encoding of updatingKey is invalid"),
    SIGNING_KEY_INVALID_ENCODING(6, "encoding of signingKey is invalid"),
    VALID_FROM_IN_PAST(7, "validFrom of first signing key cannot be in the past"),
    SIGNATURE_INVALID(8, "signature check failed"),
    IO_EXCEPTION(9, "I/O exception occured");

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
