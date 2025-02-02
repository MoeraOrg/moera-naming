package org.moera.naming.rpc.exception;

public class ServiceException extends JsonRpcException {

    private final String errorCode;

    public ServiceException(ServiceError serviceError) {
        super(serviceError.getRpcCode(), serviceError.getErrorCode() + ": " + serviceError.getMessage());
        this.errorCode = serviceError.getErrorCode();
    }

    public String getErrorCode() {
        return errorCode;
    }

}
