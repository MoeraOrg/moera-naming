package org.moera.naming.registry;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.moera.lib.Rules;
import org.moera.lib.crypto.CryptoException;
import org.moera.lib.crypto.CryptoUtil;
import org.moera.lib.naming.Fingerprints;
import org.moera.lib.naming.NamingError;
import org.moera.lib.naming.types.OperationStatus;
import org.moera.lib.util.LogUtil;
import org.moera.naming.data.Operation;
import org.moera.naming.data.OperationRepository;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.SigningKey;
import org.moera.naming.rpc.ServiceException;
import org.moera.naming.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class Registry {

    private static final Logger log = LoggerFactory.getLogger(Registry.class);

    @Inject
    private Storage storage;

    @Inject
    private OperationRepository operationRepository;

    @Transactional
    public UUID addOperation(
            String name,
            int generation,
            String nodeUri,
            byte[] signature,
            byte[] updatingKey,
            byte[] signingKey,
            Timestamp validFrom,
            byte[] previousDigest) {

        Operation operation = new Operation(name, generation, nodeUri, signature, updatingKey, signingKey,
                validFrom, previousDigest);
        operationRepository.save(operation);

        log.info("Added operation {}", operation.getId());
        return operation.getId();
    }

    @Transactional
    public void executeOperation(Operation operation) {
        try {
            log.info("Executing operation {}", operation.getId());
            executeOperation(
                    operation.getName(),
                    operation.getGeneration(),
                    operation.getNodeUri(),
                    operation.getSignature(),
                    operation.getUpdatingKey(),
                    operation.getSigningKey(),
                    operation.getValidFrom(),
                    operation.getPreviousDigest());
            log.info("Operation {} SUCCEEDED", operation.getId());
            operation.setStatus(OperationStatus.SUCCEEDED);
        } catch (ServiceException e) {
            log.warn("Operation {} FAILED, error code = {}", operation.getId(), e.getErrorCode());
            operation.setStatus(OperationStatus.FAILED);
            operation.setErrorCode(e.getErrorCode());
        }
        operation.setCompleted(Util.now());
        operationRepository.save(operation);
    }

    private void executeOperation(
            String name,
            int generation,
            String nodeUri,
            byte[] signature,
            byte[] updatingKey,
            byte[] signingKey,
            Timestamp validFrom,
            byte[] previousDigest) {

        log.debug("Executing operation on name = {}, generation = {}", LogUtil.format(name), generation);

        RegisteredName latest = storage.get(name, generation);
        validateDigest(latest, previousDigest);
        if (latest == null) {
            log.debug("Registering a new name");

            putNew(new RegisteredName(name, generation), updatingKey, nodeUri, signingKey, validFrom);
        } else {
            log.debug("Updating the name");

            SigningKey latestKey = storage.getLatestKey(
                    latest.getNameGeneration().getName(), latest.getNameGeneration().getGeneration());
            validateSignature(latest, latestKey, updatingKey, nodeUri, signingKey, validFrom, previousDigest,
                    signature);
            putExisting(latest, updatingKey, nodeUri, signingKey, validFrom);
        }
    }

    private void putNew(
            RegisteredName target,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Timestamp validFrom) {

        log.info("putNew(): name = {}, generation = {}",
                LogUtil.format(target.getNameGeneration().getName()), target.getNameGeneration().getGeneration());

        if (!ObjectUtils.isEmpty(nodeUri)) {
            target.setNodeUri(nodeUri);
        }
        if (updatingKey == null) {
            throw new ServiceException(NamingError.UPDATING_KEY_EMPTY);
        }
        target.setUpdatingKey(updatingKey);
        SigningKey targetKey = null;
        if (signingKey != null) {
            if (validFrom.before(target.getCreated())) {
                throw new ServiceException(NamingError.VALID_FROM_BEFORE_CREATED);
            }

            targetKey = new SigningKey();
            targetKey.setSigningKey(signingKey);
            targetKey.setValidFrom(validFrom);
        }
        target.setDigest(getDigest(target, targetKey));
        target = storage.save(target);
        if (targetKey != null) {
            targetKey.setRegisteredName(target);
            storage.save(targetKey);
        }
    }

    private void putExisting(
            RegisteredName target,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Timestamp validFrom) {

        log.info("putExisting(): name = {}, generation = {}",
                LogUtil.format(target.getNameGeneration().getName()), target.getNameGeneration().getGeneration());

        if (!ObjectUtils.isEmpty(nodeUri)) {
            target.setNodeUri(nodeUri);
        }
        if (updatingKey != null) {
            target.setUpdatingKey(updatingKey);
        }
        SigningKey targetKey = null;
        if (signingKey != null) {
            if (validFrom.before(target.getCreated())) {
                throw new ServiceException(NamingError.VALID_FROM_BEFORE_CREATED);
            }
            if (validFrom.before(Timestamp.from(Instant.now().minus(Rules.VALID_FROM_IN_PAST)))) {
                throw new ServiceException(NamingError.VALID_FROM_TOO_FAR_IN_PAST);
            }

            targetKey = new SigningKey();
            targetKey.setSigningKey(signingKey);
            targetKey.setValidFrom(validFrom);
        }
        target.setDigest(getDigest(target, targetKey));
        target = storage.save(target);
        if (targetKey != null) {
            targetKey.setRegisteredName(target);
            storage.save(targetKey);
        }
    }

    private void validateDigest(RegisteredName registeredName, byte[] previousDigest) {
        byte[] digest = registeredName != null ? registeredName.getDigest() : Util.EMPTY_DIGEST;
        previousDigest = previousDigest != null ? previousDigest : Util.EMPTY_DIGEST;
        if (!Arrays.equals(digest, previousDigest)) {
            throw new ServiceException(NamingError.PREVIOUS_DIGEST_INCORRECT);
        }
    }

    private void validateSignature(
            RegisteredName target,
            SigningKey latestKey,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Timestamp validFrom,
            byte[] previousDigest,
            byte[] signature) {

        try {
            byte[] eSigningKey = null;
            Timestamp eValidFrom = Util.toTimestamp(0L);

            if (signingKey != null) {
                eSigningKey = signingKey;
                eValidFrom = validFrom;
            } else if (latestKey != null) {
                eSigningKey = latestKey.getSigningKey();
                eValidFrom = latestKey.getValidFrom();
            }

            byte[] putCall = Fingerprints.putCall(
                target.getNameGeneration().getName(),
                target.getNameGeneration().getGeneration(),
                updatingKey != null ? updatingKey : target.getUpdatingKey(),
                nodeUri != null ? nodeUri : target.getNodeUri(),
                eSigningKey,
                eValidFrom,
                previousDigest
            );

            if (log.isDebugEnabled()) {
                log.debug(
                    "Verifying signature: fingerprint = {}, signature = {}, target updatingKey = {}",
                    LogUtil.format(putCall), LogUtil.format(signature), LogUtil.format(target.getUpdatingKey())
                );
            }

            if (!CryptoUtil.verifySignature(putCall, signature, target.getUpdatingKey())) {
                throw new ServiceException(NamingError.SIGNATURE_INVALID);
            }
        } catch (CryptoException e) {
            log.error("Crypto exception:", e);
            throw new ServiceException(NamingError.CRYPTO_EXCEPTION);
        }
    }

    private byte[] getDigest(RegisteredName registeredName, SigningKey signingKey) {
        if (registeredName == null) {
            return Util.EMPTY_DIGEST;
        }
        try {
            return CryptoUtil.digest(Fingerprints.putCall(
                registeredName.getNameGeneration().getName(),
                registeredName.getNameGeneration().getGeneration(),
                registeredName.getUpdatingKey(),
                registeredName.getNodeUri(),
                signingKey != null ? signingKey.getSigningKey() : null,
                signingKey != null ? signingKey.getValidFrom() : Util.toTimestamp(0L),
                registeredName.getDigest()
            ));
        } catch (CryptoException e) {
            log.error("Crypto exception:", e);
            throw new ServiceException(NamingError.CRYPTO_EXCEPTION);
        }
    }

    public Operation getOperation(UUID operationId) {
        return operationRepository.findById(operationId).orElse(null);
    }

    public List<RegisteredName> getAll(Timestamp at, int page, int size) {
        if (page < 0) {
            throw new ServiceException(NamingError.PAGE_INCORRECT);
        }
        if (size < 1) {
            throw new ServiceException(NamingError.PAGE_SIZE_INCORRECT);
        }
        if (size > Rules.PAGE_MAX_SIZE) {
            throw new ServiceException(NamingError.PAGE_SIZE_TOO_LARGE);
        }
        return storage.getAll(at, page, size);
    }

    public List<RegisteredName> getAllNewer(Timestamp at, int page, int size) {
        if (page < 0) {
            throw new ServiceException(NamingError.PAGE_INCORRECT);
        }
        if (size < 1) {
            throw new ServiceException(NamingError.PAGE_SIZE_INCORRECT);
        }
        if (size > Rules.PAGE_MAX_SIZE) {
            throw new ServiceException(NamingError.PAGE_SIZE_TOO_LARGE);
        }
        return storage.getAllNewer(at, page, size);
    }

    public RegisteredName get(String name, int generation) {
        return storage.get(name, generation);
    }

    public RegisteredName getSimilar(String name) {
        return storage.getSimilar(name.toLowerCase());
    }

    public List<SigningKey> getAllKeys(String name, int generation) {
        return storage.getAllKeys(name, generation);
    }

    public SigningKey getLatestKey(String name, int generation) {
        return storage.getLatestKey(name, generation);
    }

    public SigningKey getKeyValidAt(String name, int generation, Timestamp at) {
        return storage.getKeyValidAt(name, generation, at);
    }

}
