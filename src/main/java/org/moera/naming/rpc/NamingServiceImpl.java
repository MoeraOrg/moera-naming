package org.moera.naming.rpc;

import javax.inject.Inject;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import org.moera.naming.data.NameGeneration;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.Storage;
import org.moera.naming.rpc.exception.ServiceError;
import org.moera.naming.rpc.exception.ServiceException;
import org.moera.naming.util.Util;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@AutoJsonRpcServiceImpl
public class NamingServiceImpl implements NamingService {

    @Inject
    private Storage storage;

    @Override
    public long put(
            String name,
            boolean newGeneration,
            String updatingKey,
            String nodeUri,
            String signingKey,
            Long validFrom,
            String signature) {

        if (StringUtils.isEmpty(name)) {
            throw new ServiceException(ServiceError.NAME_EMPTY);
        }
        RegisteredName latest = storage.getLatestGeneration(name);
        RegisteredName target;
        if (newGeneration || isForceNewGeneration(latest)) {
            int generation = latest == null ? 0 : latest.getNameGeneration().getGeneration() + 1;
            target = new RegisteredName();
            target.setNameGeneration(new NameGeneration(name, generation));
        } else {
            target = latest;
        }
        storage.save(target);

        return 0;
    }

    private boolean isForceNewGeneration(RegisteredName latest) {
        if (latest == null) {
            return true;
        }
        return latest.getDeadline().before(Util.now());
    }

}
