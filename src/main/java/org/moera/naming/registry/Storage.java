package org.moera.naming.registry;

import java.sql.Timestamp;
import java.util.List;
import jakarta.inject.Inject;

import org.moera.naming.data.NameGeneration;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.RegisteredNameRepository;
import org.moera.naming.data.SigningKey;
import org.moera.naming.data.SigningKeyRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class Storage {

    @Inject
    private RegisteredNameRepository registeredNameRepository;

    @Inject
    private SigningKeyRepository signingKeyRepository;

    public List<RegisteredName> getAll(Timestamp at, int page, int size) {
        return registeredNameRepository.findAllAt(at, PageRequest.of(page, size));
    }

    public List<RegisteredName> getAllNewer(Timestamp at, int page, int size) {
        return registeredNameRepository.findAllNewer(at, PageRequest.of(page, size));
    }

    public RegisteredName get(String name, int generation) {
        return registeredNameRepository.findById(new NameGeneration(name, generation)).orElse(null);
    }

    public RegisteredName getSimilar(String name) {
        List<RegisteredName> names = registeredNameRepository.findSimilar(name, PageRequest.of(0, 1));
        return names.isEmpty() ? null : names.get(0);
    }

    public List<SigningKey> getAllKeys(String name, int generation) {
        return signingKeyRepository.findAllKeys(new NameGeneration(name, generation), Pageable.unpaged());
    }

    public SigningKey getLatestKey(String name, int generation) {
        List<SigningKey> keys = signingKeyRepository.findAllKeys(new NameGeneration(name, generation),
                PageRequest.of(0, 1));
        return keys.isEmpty() ? null : keys.get(0);
    }

    public SigningKey getKeyValidAt(String name, int generation, Timestamp at) {
        List<SigningKey> keys = signingKeyRepository.findKeysValidBefore(new NameGeneration(name, generation), at,
                PageRequest.of(0, 1));
        return keys.isEmpty() ? null : keys.get(0);
    }

    public RegisteredName save(RegisteredName registeredName) {
        return registeredNameRepository.save(registeredName);
    }

    public SigningKey save(SigningKey signingKey) {
        return signingKeyRepository.save(signingKey);
    }

}
