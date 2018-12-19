package org.moera.naming.data;

import java.util.List;
import javax.inject.Inject;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class Storage {

    @Inject
    private RegisteredNameRepository registeredNameRepository;

    @Inject
    private SigningKeyRepository signingKeyRepository;

    public RegisteredName getLatestGeneration(String name) {
        List<RegisteredName> generations = registeredNameRepository.findAllGenerations(name, PageRequest.of(0, 1));
        return generations.isEmpty() ? null : generations.get(0);
    }

    public SigningKey getLatestKey(NameGeneration nameGeneration) {
        List<SigningKey> keys = signingKeyRepository.findAllKeys(nameGeneration, PageRequest.of(0, 1));
        return keys.isEmpty() ? null : keys.get(0);
    }

    public RegisteredName save(RegisteredName registeredName) {
        return registeredNameRepository.save(registeredName);
    }

    public SigningKey save(SigningKey signingKey) {
        return signingKeyRepository.save(signingKey);
    }

}
