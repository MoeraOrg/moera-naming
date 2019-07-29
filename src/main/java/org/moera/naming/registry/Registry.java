package org.moera.naming.registry;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.moera.commons.crypto.CryptoException;
import org.moera.commons.crypto.CryptoUtil;
import org.moera.commons.util.LogUtil;
import org.moera.commons.util.Util;
import org.moera.naming.data.NameGeneration;
import org.moera.naming.data.Operation;
import org.moera.naming.data.OperationRepository;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.SigningKey;
import org.moera.naming.rpc.OperationStatus;
import org.moera.naming.rpc.PutCallFingerprint;
import org.moera.naming.rpc.Rules;
import org.moera.naming.rpc.exception.ServiceError;
import org.moera.naming.rpc.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class Registry {

    private static Logger log = LoggerFactory.getLogger(Registry.class);

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

        RegisteredName latest = storage.getLatestGeneration(name);
        validateDigest(latest, previousDigest);
        if (isForceNewGeneration(latest, signature)) {
            log.debug("Forcing new generation");

            RegisteredName target = newGeneration(latest, name, generation);
            putNew(target, updatingKey, nodeUri, signingKey, validFrom);
        } else {
            if (generation != latest.getNameGeneration().getGeneration()) {
                RegisteredName target = newGeneration(latest, name, generation);
                validateSignature(target, null, updatingKey, nodeUri, signingKey, validFrom, previousDigest,
                        signature);
                putNew(target, updatingKey, nodeUri, signingKey, validFrom);
            } else {
                SigningKey latestKey = storage.getLatestKey(latest.getNameGeneration());
                validateSignature(latest, latestKey, updatingKey, nodeUri, signingKey, validFrom, previousDigest,
                        signature);
                putExisting(latest, updatingKey, nodeUri, signingKey, validFrom);
            }
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

        if (!StringUtils.isEmpty(nodeUri)) {
            target.setNodeUri(nodeUri);
        }
        if (updatingKey == null) {
            throw new ServiceException(ServiceError.UPDATING_KEY_EMPTY);
        }
        target.setUpdatingKey(updatingKey);
        SigningKey targetKey = null;
        if (signingKey != null) {
            if (validFrom.before(target.getCreated())) {
                throw new ServiceException(ServiceError.VALID_FROM_BEFORE_CREATED);
            }

            targetKey = new SigningKey();
            targetKey.setSigningKey(signingKey);
            targetKey.setValidFrom(validFrom);
        }
        target.setDeadline(Timestamp.from(Instant.now().plus(Rules.REGISTRATION_DURATION)));
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

        if (!StringUtils.isEmpty(nodeUri)) {
            target.setNodeUri(nodeUri);
        }
        if (updatingKey != null) {
            target.setUpdatingKey(updatingKey);
        }
        SigningKey targetKey = null;
        if (signingKey != null) {
            if (validFrom.before(target.getCreated())) {
                throw new ServiceException(ServiceError.VALID_FROM_BEFORE_CREATED);
            }
            if (validFrom.before(Timestamp.from(Instant.now().minus(Rules.VALID_FROM_IN_PAST)))) {
                throw new ServiceException(ServiceError.VALID_FROM_TOO_FAR_IN_PAST);
            }

            targetKey = new SigningKey();
            targetKey.setSigningKey(signingKey);
            targetKey.setValidFrom(validFrom);
        }
        target.setDeadline(Timestamp.from(Instant.now().plus(Rules.REGISTRATION_DURATION)));
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
            throw new ServiceException(ServiceError.PREVIOUS_DIGEST_INCORRECT);
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
            long eValidFrom = 0;

            if (signingKey != null) {
                eSigningKey = signingKey;
                eValidFrom = validFrom.toInstant().getEpochSecond();
            } else if (latestKey != null) {
                eSigningKey = latestKey.getSigningKey();
                eValidFrom = latestKey.getValidFrom().toInstant().getEpochSecond();
            }

            Object putCall = new PutCallFingerprint(
                    target.getNameGeneration().getName(),
                    target.getNameGeneration().getGeneration(),
                    updatingKey != null ? updatingKey : target.getUpdatingKey(),
                    nodeUri != null ? nodeUri : target.getNodeUri(),
                    eSigningKey,
                    eValidFrom,
                    previousDigest);

            if (log.isDebugEnabled()) {
                log.debug("Verifying signature: fingerprint = {}, signature = {}, target updatingKey = {}",
                        LogUtil.format(CryptoUtil.fingerprint(putCall)), LogUtil.format(signature),
                        LogUtil.format(target.getUpdatingKey()));
            }

            if (!CryptoUtil.verify(putCall, signature, target.getUpdatingKey())) {
                throw new ServiceException(ServiceError.SIGNATURE_INVALID);
            }
        } catch (CryptoException e) {
            log.error("Crypto exception:", e);
            throw new ServiceException(ServiceError.CRYPTO_EXCEPTION);
        }
    }

    private boolean isForceNewGeneration(RegisteredName latest, byte[] signature) {
        if (latest == null) {
            return true;
        }
        return latest.getDeadline().before(Util.now()) && StringUtils.isEmpty(signature);
    }

    private RegisteredName newGeneration(RegisteredName latest, String name, int generation) {
        int targetGeneration = latest == null ? 0 : latest.getNameGeneration().getGeneration() + 1;
        if (targetGeneration != generation) {
            throw new ServiceException(ServiceError.GENERATION_NOT_NEXT);
        }
        RegisteredName target = new RegisteredName();
        target.setNameGeneration(new NameGeneration(name, generation));
        return target;
    }

    private byte[] getDigest(RegisteredName registeredName, SigningKey signingKey) {
        if (registeredName == null) {
            return Util.EMPTY_DIGEST;
        }
        try {
            return CryptoUtil.digest(new PutCallFingerprint(
                    registeredName.getNameGeneration().getName(),
                    registeredName.getNameGeneration().getGeneration(),
                    registeredName.getUpdatingKey(),
                    registeredName.getNodeUri(),
                    signingKey != null ? signingKey.getSigningKey() : null,
                    signingKey != null ? signingKey.getValidFrom().toInstant().getEpochSecond() : 0,
                    registeredName.getDigest()));
        } catch (CryptoException e) {
            log.error("Crypto exception:", e);
            throw new ServiceException(ServiceError.CRYPTO_EXCEPTION);
        }
    }

    public Operation getOperation(UUID operationId) {
        return operationRepository.findById(operationId).orElse(null);
    }

    public RegisteredName get(String name, int generation) {
        return storage.get(name, generation);
    }

    public RegisteredName getLatestGeneration(String name) {
        return storage.getLatestGeneration(name);
    }

    public Integer getLatestGenerationNumber(String name) {
        return storage.getLatestGenerationNumber(name);
    }

    public SigningKey getLatestKey(NameGeneration nameGeneration) {
        return storage.getLatestKey(nameGeneration);
    }

}
