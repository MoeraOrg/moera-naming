package org.moera.naming.rpc;

import javax.inject.Inject;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import org.moera.naming.data.NameGeneration;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.RegisteredNameRepository;
import org.moera.naming.data.SigningKeyRepository;
import org.springframework.stereotype.Component;

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
            Boolean newGeneration,
            String updatingKey,
            String nodeUri,
            String signingKey,
            Long validFrom,
            String signature) {

        RegisteredName registeredName = new RegisteredName();
        registeredName.setNameGeneration(new NameGeneration(name));
        registeredNameRepository.save(registeredName);

        return 0;
    }

}
