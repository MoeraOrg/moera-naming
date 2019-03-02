package org.moera.naming.rpc;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import org.moera.commons.util.CryptoUtil;
import org.moera.commons.util.Util;
import org.moera.naming.Config;
import org.moera.naming.data.NameGeneration;
import org.moera.naming.data.Operation;
import org.moera.naming.data.OperationRepository;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.SigningKey;
import org.moera.naming.data.Storage;
import org.moera.naming.rpc.exception.ServiceError;
import org.moera.naming.rpc.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@AutoJsonRpcServiceImpl
public class NamingServiceImpl implements NamingService {

    private static Logger log = LoggerFactory.getLogger(NamingServiceImpl.class);

    @Inject
    private Config config;

    @Inject
    private Storage storage;

    @Inject
    private OperationRepository operationRepository;

    private int operationRunCapacity;

    @PostConstruct
    public void init() {
        operationRunCapacity = config.getMaxOperationRate();
    }

    @Override
    public UUID put(
            String name,
            boolean newGeneration,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Long validFrom,
            byte[] signature) {

        if (StringUtils.isEmpty(name)) {
            throw new ServiceException(ServiceError.NAME_EMPTY);
        }
        if (name.length() > Rules.NAME_MAX_LENGTH) {
            throw new ServiceException(ServiceError.NAME_TOO_LONG);
        }
        if (!Rules.NAME_PATTERN.matcher(name).matches()) {
            throw new ServiceException(ServiceError.NAME_FORBIDDEN_CHARS);
        }
        if (updatingKey != null) {
            if (updatingKey.length != Rules.PUBLIC_KEY_LENGTH) {
                throw new ServiceException(ServiceError.UPDATING_KEY_WRONG_LENGTH);
            }
        } else if (newGeneration) { // this case we can detect early
            throw new ServiceException(ServiceError.UPDATING_KEY_EMPTY);
        }
        if (nodeUri != null && nodeUri.length() > Rules.NODE_URI_MAX_LENGTH) {
            throw new ServiceException(ServiceError.NODE_URI_TOO_LONG);
        }
        if (signingKey != null) {
            if (signingKey.length != Rules.PUBLIC_KEY_LENGTH) {
                throw new ServiceException(ServiceError.SIGNING_KEY_WRONG_LENGTH);
            }
            if (validFrom == null) {
                throw new ServiceException(ServiceError.VALID_FROM_EMPTY);
            }
        }
        Timestamp validFromT = validFrom != null ? Timestamp.from(Instant.ofEpochSecond(validFrom)) : null;
        if (signature != null && signature.length > Rules.SIGNATURE_MAX_LENGTH) {
            throw new ServiceException(ServiceError.SIGNATURE_TOO_LONG);
        }

        return addOperation(name, newGeneration, nodeUri, signature, updatingKey, signingKey, validFromT);
    }

    @Transactional
    private UUID addOperation(
            String name,
            boolean newGeneration,
            String nodeUri,
            byte[] signature,
            byte[] updatingKey,
            byte[] signingKey,
            Timestamp validFrom) {

        Operation operation = new Operation(name, newGeneration, nodeUri, signature, updatingKey, signingKey, validFrom);
        operationRepository.save(operation);
        return operation.getId();
    }

    @Scheduled(fixedDelayString = "PT1M") // every minute
    public void runOperationQueue() {
        operationRunCapacity += config.getAverageOperationRate();
        if (operationRunCapacity > config.getMaxOperationRate()) {
            operationRunCapacity = config.getMaxOperationRate();
        }

        List<Operation> operations = operationRepository.findAllByStatusOrderByAdded(OperationStatus.ADDED,
                PageRequest.of(0, operationRunCapacity));
        operationRunCapacity -= operations.size();
        operations.forEach(this::executeOperation);
    }

    @Transactional
    private void executeOperation(Operation operation) {
        try {
            int generation = executeOperation(
                    operation.getName(),
                    operation.isNewGeneration(),
                    operation.getNodeUri(),
                    operation.getSignature(),
                    operation.getUpdatingKey(),
                    operation.getSigningKey(),
                    operation.getValidFrom());
            operation.setStatus(OperationStatus.SUCCEEDED);
            operation.setGeneration(generation);
        } catch (ServiceException e) {
            operation.setStatus(OperationStatus.FAILED);
            operation.setErrorCode(e.getErrorCode());
        }
        operation.setCompleted(Util.now());
        operationRepository.save(operation);
    }

    private int executeOperation(
            String name,
            boolean newGeneration,
            String nodeUri,
            byte[] signature,
            byte[] updatingKey,
            byte[] signingKey,
            Timestamp validFrom) {

        int generation;
        RegisteredName latest = storage.getLatestGeneration(name);
        if (isForceNewGeneration(latest, signature)) {
            RegisteredName target = newGeneration(latest, name);
            generation = target.getNameGeneration().getGeneration();
            putNew(target, updatingKey, nodeUri, signingKey, validFrom);
        } else {
            if (newGeneration) {
                RegisteredName target = newGeneration(latest, name);
                generation = target.getNameGeneration().getGeneration();
                validateSignature(target, null, updatingKey, nodeUri, signingKey, validFrom, signature);
                putNew(target, updatingKey, nodeUri, signingKey, validFrom);
            } else {
                generation = latest.getNameGeneration().getGeneration();
                SigningKey latestKey = storage.getLatestKey(latest.getNameGeneration());
                validateSignature(latest, latestKey, updatingKey, nodeUri, signingKey, validFrom, signature);
                putExisting(latest, updatingKey, nodeUri, signingKey, validFrom);
            }
        }
        return generation;
    }

    private void putNew(
            RegisteredName target,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Timestamp validFrom) {

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
            targetKey.setRegisteredName(target);
        }
        target.setDeadline(Timestamp.from(Instant.now().plus(Rules.REGISTRATION_DURATION)));
        storage.save(target);
        if (targetKey != null) {
            storage.save(targetKey);
        }
    }

    private void putExisting(
            RegisteredName target,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Timestamp validFrom) {

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
            targetKey.setRegisteredName(target);
        }
        target.setDeadline(Timestamp.from(Instant.now().plus(Rules.REGISTRATION_DURATION)));
        storage.save(target);
        if (targetKey != null) {
            storage.save(targetKey);
        }
    }

    private void validateSignature(
            RegisteredName target,
            SigningKey latestKey,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Timestamp validFrom,
            byte[] signature) {

        try {
            byte[] eSigningKey = null;
            long eValidFrom = 0;

            if (signingKey != null) {
                eSigningKey = signingKey;
                eValidFrom = validFrom.getTime();
            } else if (latestKey != null) {
                eSigningKey = latestKey.getSigningKey();
                eValidFrom = latestKey.getValidFrom().getTime();
            }

            byte[] signatureData = new PutSignatureDataBuilder(
                    target.getNameGeneration().getName(),
                    updatingKey != null ? updatingKey : target.getUpdatingKey(),
                    nodeUri != null ? nodeUri : target.getNodeUri(),
                    target.getDeadline().getTime(),
                    eSigningKey,
                    eValidFrom).toBytes();

            Signature sign = Signature.getInstance(Rules.SIGNATURE_ALGORITHM, "BC");
            sign.initVerify(CryptoUtil.toPublicKey(target.getUpdatingKey()));
            sign.update(signatureData);
            if (!sign.verify(signature)) {
                throw new ServiceException(ServiceError.SIGNATURE_INVALID);
            }
        } catch (GeneralSecurityException e) {
            log.error("Crypto exception:", e);
            throw new ServiceException(ServiceError.CRYPTO_EXCEPTION);
        } catch (IOException e) {
            throw new ServiceException(ServiceError.IO_EXCEPTION);
        }
    }

    private boolean isForceNewGeneration(RegisteredName latest, byte[] signature) {
        if (latest == null) {
            return true;
        }
        return latest.getDeadline().before(Util.now()) && StringUtils.isEmpty(signature);
    }

    private RegisteredName newGeneration(RegisteredName latest, String name) {
        int generation = latest == null ? 0 : latest.getNameGeneration().getGeneration() + 1;
        RegisteredName target = new RegisteredName();
        target.setNameGeneration(new NameGeneration(name, generation));
        return target;
    }

    @Override
    public OperationStatusInfo getStatus(UUID operationId) {
        Operation operation = operationRepository.findById(operationId).orElse(null);
        if (operation == null) {
            OperationStatusInfo info = new OperationStatusInfo();
            info.setErrorCode("unknown");
            return info;
        }
        return operation.toOperationStatusInfo();
    }

    @Override
    public RegisteredNameInfo getCurrent(String name, int generation) {
        RegisteredName registeredName = storage.get(name, generation);
        if (registeredName == null) {
            return null;
        }
        Integer latestGeneration = storage.getLatestGenerationNumber(name);
        return getRegisteredNameInfo(registeredName, latestGeneration == generation);
    }

    @Override
    public RegisteredNameInfo getCurrentForLatest(String name) {
        RegisteredName registeredName = storage.getLatestGeneration(name);
        if (registeredName == null) {
            return null;
        }
        return getRegisteredNameInfo(registeredName, true);
    }

    private RegisteredNameInfo getRegisteredNameInfo(RegisteredName registeredName, boolean latest) {
        RegisteredNameInfo info = new RegisteredNameInfo();
        info.setName(registeredName.getNameGeneration().getName());
        info.setGeneration(registeredName.getNameGeneration().getGeneration());
        info.setLatest(latest);
        info.setUpdatingKey(Util.base64encode(registeredName.getUpdatingKey()));
        info.setNodeUri(registeredName.getNodeUri());
        info.setDeadline(registeredName.getDeadline().getTime());
        SigningKey latestKey = storage.getLatestKey(registeredName.getNameGeneration());
        if (latestKey != null) {
            info.setSigningKey(Util.base64encode(latestKey.getSigningKey()));
            info.setValidFrom(latestKey.getValidFrom().getTime());
        }
        return info;
    }

}
