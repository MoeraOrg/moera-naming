package org.moera.naming.registry;

import java.sql.Timestamp;
import java.util.List;
import javax.inject.Inject;

import org.moera.naming.data.NameGeneration;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.RegisteredNameRepository;
import org.moera.naming.data.SigningKey;
import org.moera.naming.data.SigningKeyRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class Storage {

    @Inject
    private RegisteredNameRepository registeredNameRepository;

    @Inject
    private SigningKeyRepository signingKeyRepository;

    public RegisteredName get(String name, int generation) {
        return registeredNameRepository.findById(new NameGeneration(name, generation)).orElse(null);
    }

    public RegisteredName getLatestGeneration(String name) {
        List<RegisteredName> generations = registeredNameRepository.findAllGenerations(name, PageRequest.of(0, 1));
        return generations.isEmpty() ? null : generations.get(0);
    }

    public Integer getLatestGenerationNumber(String name) {
        return registeredNameRepository.findLatestGenerationNumber(name);
    }

    public SigningKey getLatestKey(NameGeneration nameGeneration) {
        List<SigningKey> keys = signingKeyRepository.findAllKeys(nameGeneration, PageRequest.of(0, 1));
        return keys.isEmpty() ? null : keys.get(0);
    }

    public SigningKey getKeyValidAt(NameGeneration nameGeneration, Timestamp at) {
        List<SigningKey> keys = signingKeyRepository.findKeysValidBefore(nameGeneration, at, PageRequest.of(0, 1));
        return keys.isEmpty() ? null : keys.get(0);
    }

    public RegisteredName save(RegisteredName registeredName) {
        return registeredNameRepository.save(registeredName);
    }

    public SigningKey save(SigningKey signingKey) {
        return signingKeyRepository.save(signingKey);
    }

}
