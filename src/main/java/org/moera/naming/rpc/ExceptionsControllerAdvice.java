package org.moera.naming.rpc;

import org.moera.lib.jsonrpc.JsonRpcError;
import org.moera.lib.jsonrpc.JsonRpcResponse;
import org.moera.lib.naming.NamingError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionsControllerAdvice {

    private final ThreadLocal<Object> requestId = new ThreadLocal<>();

    public void setRequestId(Object requestId) {
        this.requestId.set(requestId);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public JsonRpcResponse exception(Throwable e) {
        return new JsonRpcResponse(requestId.get(), JsonRpcError.PARSE_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<JsonRpcResponse> jsonRpcException(JsonRpcException e) {
        var response = new JsonRpcResponse(requestId.get(), e.getRpcCode(), e.getMessage());
        if (e.getRpcCode() == JsonRpcError.INVALID_REQUEST.getCode()) {
            return ResponseEntity.badRequest().body(response);
        }
        if (e.getRpcCode() == JsonRpcError.METHOD_NOT_FOUND.getCode()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler
    public ResponseEntity<JsonRpcResponse> serviceException(ServiceException e) {
        var response = new JsonRpcResponse(requestId.get(), e.getRpcCode(), e.getMessage());
        if (e.getRpcCode() == NamingError.ENDPOINT_WRONG.getRpcCode()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public JsonRpcResponse methodNotSupported(HttpRequestMethodNotSupportedException e) {
        return new JsonRpcResponse(requestId.get(), NamingError.ENDPOINT_WRONG);
    }

}
