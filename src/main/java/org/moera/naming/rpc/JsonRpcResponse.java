package org.moera.naming.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.moera.naming.rpc.exception.JsonRpcError;
import org.moera.naming.rpc.exception.ServiceError;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {

    private final String jsonrpc = "2.0";
    private Object result;
    private JsonRpcError error;
    private Object id;

    public JsonRpcResponse(Object id, Object result) {
        this.id = id;
        this.result = result;
    }

    public JsonRpcResponse(Object id, JsonRpcError error) {
        this.id = id;
        this.error = error;
    }

    public JsonRpcResponse(Object id, ServiceError error) {
        this.id = id;
        this.error = new JsonRpcError(error.getRpcCode(), error.getErrorCode() + ": " + error.getMessage());
    }

    public JsonRpcResponse(Object id, int code, String message) {
        this.id = id;
        this.error = new JsonRpcError(code, message);
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public JsonRpcError getError() {
        return error;
    }

    public void setError(JsonRpcError error) {
        this.error = error;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

}
