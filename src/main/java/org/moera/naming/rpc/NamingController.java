package org.moera.naming.rpc;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import org.moera.naming.Config;
import org.moera.naming.rpc.exception.ServiceErrorResolver;
import org.moera.naming.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NamingController {

    private static final Logger log = LoggerFactory.getLogger(NamingController.class);

    private JsonRpcServer jsonRpcServer;

    @Inject
    private Config config;

    @Inject
    private NamingService namingService;

    @Inject
    private ServiceErrorResolver serviceErrorResolver;

    @PostConstruct
    protected void init() {
        jsonRpcServer = new JsonRpcServer(new ObjectMapper(), namingService, NamingService.class);
        jsonRpcServer.setAllowExtraParams(true);
        jsonRpcServer.setAllowLessParams(true);
        jsonRpcServer.setErrorResolver(serviceErrorResolver);

        if (config.isMockNetworkLatency()) {
            log.info("Emulation of network latency is enabled."
                    + " Random delay of 200ms up to 2s will be added to all responses");
        }
    }

    @CrossOrigin("*")
    @PostMapping("/moera-naming")
    public void naming(HttpServletRequest request, HttpServletResponse response) throws IOException {
        networkLatency();
        jsonRpcServer.handle(request, response);
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
