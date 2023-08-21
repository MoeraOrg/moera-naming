package org.moera.naming.rpc.exception;

import java.lang.reflect.Method;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.DefaultErrorResolver;
import com.googlecode.jsonrpc4j.ErrorResolver;
import org.springframework.stereotype.Component;

@Component
public class ServiceErrorResolver implements ErrorResolver {

    @Override
    public JsonError resolveError(Throwable throwable, Method method, List<JsonNode> list) {
        if (throwable instanceof ServiceException e) {
            return new JsonError(e.getRpcCode(), e.getMessage(), null);
        }
        return DefaultErrorResolver.INSTANCE.resolveError(throwable, method, list);
    }

}
