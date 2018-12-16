package org.moera.naming.rpc;

import java.util.List;
import javax.inject.Inject;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import org.moera.naming.data.NameGeneration;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.RegisteredNameRepository;
import org.moera.naming.data.SigningKeyRepository;
import org.moera.naming.data.exception.NameEmptyException;
import org.moera.naming.util.Util;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@AutoJsonRpcServiceImpl
public class NamingServiceImpl implements NamingService {

    @Inject
    private RegisteredNameRepository registeredNameRepository;

    @Inject
    private SigningKeyRepository signingKeyRepository;

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
            throw new NameEmptyException();
        }
        NameGeneration latest = getLatestGeneration(name);
        RegisteredName record;
        if (newGeneration || isForceNewGeneration(latest)) {
            record = new RegisteredName();
            NameGeneration future = new NameGeneration(name, latest == null ? 0 : latest.getGeneration() + 1);
            record.setNameGeneration(future);
        } else {
            record = getRecord(latest);
        }
        registeredNameRepository.save(record);

        return 0;
    }

    private boolean isForceNewGeneration(NameGeneration latest) {
        if (latest == null) {
            return true;
        }
        return getRecord(latest).getDeadline().before(Util.now());
    }

    private NameGeneration getLatestGeneration(String name) {
        List<NameGeneration> generations = registeredNameRepository.getNameGenerations(name);
        return generations.isEmpty() ? null : generations.get(0);
    }

    private RegisteredName getRecord(NameGeneration nameGeneration) {
        return registeredNameRepository.findById(nameGeneration).orElse(null);
        // FIXME Error if record == null
    }

}
