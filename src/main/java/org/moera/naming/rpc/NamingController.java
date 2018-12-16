package org.moera.naming.rpc;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NamingController {

    private JsonRpcServer jsonRpcServer;

    @Inject
    private NamingService namingService;

    @PostConstruct
    protected void init() {
        jsonRpcServer = new JsonRpcServer(new ObjectMapper(), namingService, NamingService.class);
        jsonRpcServer.setAllowExtraParams(true);
        jsonRpcServer.setAllowLessParams(true);
    }

    @PostMapping("/moera-naming")
    public void naming(HttpServletRequest request, HttpServletResponse response) throws IOException {
        jsonRpcServer.handle(request, response);
    }

}
