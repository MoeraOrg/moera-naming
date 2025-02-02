package org.moera.naming.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

import com.fasterxml.jackson.databind.JsonNode;
import org.moera.naming.Config;
import org.moera.naming.rpc.exception.JsonRpcError;
import org.moera.naming.rpc.exception.JsonRpcException;
import org.moera.naming.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NamingController {

    private static final Logger log = LoggerFactory.getLogger(NamingController.class);

    @Inject
    private Config config;

    @Inject
    private NamingService namingService;

    @Inject
    private ExceptionsControllerAdvice exceptionsControllerAdvice;

    @PostConstruct
    protected void init() {
        if (config.isMockNetworkLatency()) {
            log.info("Emulation of network latency is enabled."
                    + " Random delay of 200ms up to 2s will be added to all responses");
        }
    }

    @CrossOrigin("*")
    @PostMapping(
            value = "/moera-naming",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public JsonRpcResponse naming(@Valid @RequestBody JsonRpcRequest request) {
        networkLatency();
        if (!(request.getId() instanceof String) && !(request.getId() instanceof Integer)) {
            throw new JsonRpcException(JsonRpcError.INVALID_REQUEST);
        }
        exceptionsControllerAdvice.setRequestId(request.getId());

        if (request.getMethod() == null) {
            throw new JsonRpcException(JsonRpcError.INVALID_REQUEST);
        }
        for (Method method : namingService.getClass().getMethods()) {
            if (!request.getMethod().equals(method.getName())) {
                continue;
            }
            if (!method.isAnnotationPresent(JsonRpcMethod.class)) {
                throw new JsonRpcException(JsonRpcError.METHOD_NOT_FOUND);
            }
            JsonNode params = request.getParams();
            if (method.getParameterCount() > 0 && params == null || !params.isObject() && !params.isArray()) {
                throw new JsonRpcException(JsonRpcError.METHOD_PARAMS_INVALID);
            }
            Object[] values = new Object[method.getParameterCount()];
            try {
                for (int i = 0; i < method.getParameterCount(); i++) {
                    Parameter parameter = method.getParameters()[i];
                    String valueS = (params.isObject() ? params.get(parameter.getName()) : params.get(i)).asText();
                    Object value;
                    if (valueS == null) {
                        value = null;
                    } else if (parameter.getType().equals(String.class)) {
                        value = valueS;
                    } else if (parameter.getType().equals(Integer.class) || parameter.getType().equals(int.class)) {
                        value = Integer.valueOf(valueS);
                    } else if (parameter.getType().equals(Long.class) || parameter.getType().equals(long.class)) {
                        value = Long.valueOf(valueS);
                    } else if (parameter.getType().equals(byte[].class)) {
                        value = Util.base64decode(valueS);
                    } else if (parameter.getType().equals(UUID.class)) {
                        value = UUID.fromString(valueS);
                    } else {
                        log.error("Parameter type {} is not supported", parameter.getType().getName());
                        throw new JsonRpcException(JsonRpcError.INTERNAL_ERROR);
                    }
                    values[i] = value;
                }
            } catch (IllegalArgumentException e) {
                throw new JsonRpcException(JsonRpcError.METHOD_PARAMS_INVALID);
            }
            try {
                return new JsonRpcResponse(request.getId(), method.invoke(namingService, values));
            } catch (Exception e) {
                if (
                    e instanceof InvocationTargetException ite
                    && ite.getTargetException() instanceof JsonRpcException ex
                ) {
                    throw ex;
                }
                log.error("Error invoking method {}", method.getName(), e);
                throw new JsonRpcException(JsonRpcError.INTERNAL_ERROR);
            }
        }
        throw new JsonRpcException(JsonRpcError.METHOD_NOT_FOUND);
    }

    private void networkLatency() {
        if (!config.isMockNetworkLatency()) {
            return;
        }

        int period = Util.random(200, 2000);
        try {
            Thread.sleep(period);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}
