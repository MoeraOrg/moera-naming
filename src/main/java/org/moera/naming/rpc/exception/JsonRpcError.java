package org.moera.naming.rpc.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcError {

    public static final JsonRpcError OK = new JsonRpcError(0, "ok");
    public static final JsonRpcError PARSE_ERROR = new JsonRpcError(-32700, "JSON parse error");
    public static final JsonRpcError INVALID_REQUEST = new JsonRpcError(-32600, "invalid request");
    public static final JsonRpcError METHOD_NOT_FOUND = new JsonRpcError(-32601, "method not found");
    public static final JsonRpcError METHOD_PARAMS_INVALID = new JsonRpcError(-32602, "method parameters invalid");
    public static final JsonRpcError INTERNAL_ERROR = new JsonRpcError(-32603, "internal error");
    public static final JsonRpcError ERROR_NOT_HANDLED = new JsonRpcError(-32001, "error not handled");
    public static final JsonRpcError BULK_ERROR = new JsonRpcError(-32002, "bulk error");
    public static final int CUSTOM_SERVER_ERROR_UPPER = -32000;
    public static final int CUSTOM_SERVER_ERROR_LOWER = -32099;

    private final int code;
    private final String message;

    public JsonRpcError(int code, String message) {
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
