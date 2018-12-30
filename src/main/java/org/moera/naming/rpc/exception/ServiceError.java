package org.moera.naming.rpc.exception;

public enum ServiceError {

    NAME_EMPTY(1, "name.empty", "name is empty"),
    NAME_TOO_LONG(2, "name.too-long", "name is too long"),
    NAME_FORBIDDEN_CHARS(3, "name.forbidden-chars", "name contains forbidden characters"),
    NODE_URI_TOO_LONG(4, "node-uri.too-long", "nodeUri is too long"),
    UPDATING_KEY_EMPTY(5, "updating-key.empty", "updatingKey is empty"),
    UPDATING_KEY_TOO_LONG(6, "updating-key.too-long", "updatingKey is too long"),
    UPDATING_KEY_INVALID_ENCODING(7, "updating-key.invalid-encoding", "encoding of updatingKey is invalid"),
    SIGNING_KEY_TOO_LONG(8, "signing-key.too-long", "signingKey is too long"),
    SIGNING_KEY_INVALID_ENCODING(9, "signing-key.invalid-encoding", "encoding of signingKey is invalid"),
    VALID_FROM_EMPTY(10, "valid-from.empty", "validFrom is empty"),
    VALID_FROM_BEFORE_CREATED(11, "valid-from.before-name-created", "validFrom is before name creation"),
    VALID_FROM_TOO_FAR_IN_PAST(12, "valid-from.too-far-in-past", "validFrom is too far in the past"),
    SIGNATURE_INVALID_ENCODING(13, "signature.invalid-encoding", "encoding of signature is invalid"),
    SIGNATURE_INVALID(14, "signature.failed", "signature check failed"),
    IO_EXCEPTION(15, "io.failure", "I/O exception occured");

    private int rpcCode;
    private String errorCode;
    private String message;

    ServiceError(int rpcCode, String errorCode, String message) {
        this.rpcCode = rpcCode;
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getRpcCode() {
        return rpcCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

}
