package org.moera.naming.rpc.exception;

import java.util.Arrays;

public enum ServiceError {

    NAME_EMPTY(1, "name.empty", "name is empty"),
    NAME_TOO_LONG(2, "name.too-long", "name is too long"),
    NAME_FORBIDDEN_CHARS(3, "name.forbidden-chars", "name contains forbidden characters"),
    NODE_URI_TOO_LONG(4, "node-uri.too-long", "nodeUri is too long"),
    UPDATING_KEY_EMPTY(5, "updating-key.empty", "updatingKey is empty"),
    UPDATING_KEY_WRONG_LENGTH(6, "updating-key.wrong-length", "updatingKey has wrong length"),
    SIGNING_KEY_WRONG_LENGTH(7, "signing-key.wrong-length", "signingKey has wrong length"),
    VALID_FROM_EMPTY(8, "valid-from.empty", "validFrom is empty"),
    VALID_FROM_BEFORE_CREATED(9, "valid-from.before-name-created", "validFrom is before name creation"),
    VALID_FROM_TOO_FAR_IN_PAST(10, "valid-from.too-far-in-past", "validFrom is too far in the past"),
    SIGNATURE_TOO_LONG(11, "signature.too-long", "signature is too long"),
    SIGNATURE_INVALID(12, "signature.failed", "signature check failed"),
    CRYPTO_EXCEPTION(13, "crypto.failure", "crypto configuration exception occured"),
    PREVIOUS_DIGEST_WRONG_LENGTH(14, "previous-digest.wrong-length", "previousDigest has wrong length"),
    PREVIOUS_DIGEST_INCORRECT(15, "previous-digest.incorrect", "previousDigest differs from the current one"),
    GENERATION_NOT_SAME(16, "generation.not-same", "generation must be the same as the current one"),
    PAGE_INCORRECT(17, "page.incorrect", "page number is incorrect"),
    PAGE_SIZE_INCORRECT(18, "size.incorrect", "page size is incorrect"),
    PAGE_SIZE_TOO_LARGE(19, "size.too-large", "page size is too large");

    private final int rpcCode;
    private final String errorCode;
    private final String message;

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

    public static ServiceError forCode(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values()).filter(v -> v.getErrorCode().equals(code)).findFirst().orElse(null);
    }

}
