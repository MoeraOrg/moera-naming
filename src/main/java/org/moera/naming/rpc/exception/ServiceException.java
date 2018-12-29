package org.moera.naming.rpc.exception;

public class ServiceException extends RuntimeException {

    private ServiceError serviceError;

    public ServiceException(ServiceError serviceError) {
        super(serviceError.getErrorCode() + ": " + serviceError.getMessage());
        this.serviceError = serviceError;
    }

    public int getRpcCode() {
        return serviceError.getRpcCode();
    }

}
