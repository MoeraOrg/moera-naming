package org.moera.naming.rpc;

import com.googlecode.jsonrpc4j.ErrorResolver;
import org.moera.naming.rpc.exception.ServiceError;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionsControllerAdvice {

    public static class ErrorResponse {

        private ErrorResolver.JsonError error;

        public ErrorResponse(ServiceError error) {
            this.error = new ErrorResolver.JsonError(error.getRpcCode(), error.getMessage(), null);
        }

        public ErrorResolver.JsonError getError() {
            return error;
        }

        public void setError(ErrorResolver.JsonError error) {
            this.error = error;
        }

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse methodNotSupported(HttpRequestMethodNotSupportedException e) {
        return new ErrorResponse(ServiceError.ENDPOINT_WRONG);
    }

}
