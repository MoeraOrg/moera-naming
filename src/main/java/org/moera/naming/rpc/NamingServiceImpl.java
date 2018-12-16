package org.moera.naming.rpc;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import org.springframework.stereotype.Component;

@Component
@AutoJsonRpcServiceImpl
public class NamingServiceImpl implements NamingService {

    @Override
    public String hello(int value) {
        return "Hello, " + value + "!";
    }

}
