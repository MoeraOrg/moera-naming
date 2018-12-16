package org.moera.naming.rpc;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

@JsonRpcService("/moera-naming")
public interface NamingService {

    String hello(@JsonRpcParam("value") int value);

}
