package org.moera.naming.rpc;

import org.moera.lib.naming.NamingError;

public class ServiceException extends JsonRpcException {

    private final String errorCode;

    public ServiceException(NamingError namingError) {
        super(namingError.getRpcCode(), namingError.getErrorCode() + ": " + namingError.getMessage());
        this.errorCode = namingError.getErrorCode();
    }

    public String getErrorCode() {
        return errorCode;
    }

}
