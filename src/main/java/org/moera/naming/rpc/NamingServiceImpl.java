package org.moera.naming.rpc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.inject.Inject;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import org.moera.naming.data.Operation;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.SigningKey;
import org.moera.naming.registry.Registry;
import org.moera.naming.rpc.exception.ServiceError;
import org.moera.naming.rpc.exception.ServiceException;
import org.moera.naming.util.LogUtil;
import org.moera.naming.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@AutoJsonRpcServiceImpl
public class NamingServiceImpl implements NamingService {

    private static Logger log = LoggerFactory.getLogger(NamingServiceImpl.class);

    @Inject
    private Registry registry;

    @Override
    public UUID put(
            String name,
            int generation,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Long validFrom,
            byte[] previousDigest,
            byte[] signature) {

        log.info("put(): name = {}, generation = {}, updatingKey = {}, nodeUri = {},"
                + " signingKey = {}, validFrom = {}, previousDigest = {},"
                + " signature = {}",
                LogUtil.format(name), generation, LogUtil.format(updatingKey), LogUtil.format(nodeUri),
                LogUtil.format(signingKey), LogUtil.formatTimestamp(validFrom), LogUtil.format(previousDigest),
                LogUtil.format(signature));

        if (StringUtils.isEmpty(name)) {
            throw new ServiceException(ServiceError.NAME_EMPTY);
        }
        if (name.length() > Rules.NAME_MAX_LENGTH) {
            throw new ServiceException(ServiceError.NAME_TOO_LONG);
        }
        if (!Rules.isNameValid(name)) {
            throw new ServiceException(ServiceError.NAME_FORBIDDEN_CHARS);
        }
        if (updatingKey != null && updatingKey.length != Rules.PUBLIC_KEY_LENGTH) {
            throw new ServiceException(ServiceError.UPDATING_KEY_WRONG_LENGTH);
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
        if (previousDigest != null && previousDigest.length != Rules.DIGEST_LENGTH) {
            throw new ServiceException(ServiceError.PREVIOUS_DIGEST_WRONG_LENGTH);
        }
        if (signature != null && signature.length > Rules.SIGNATURE_MAX_LENGTH) {
            throw new ServiceException(ServiceError.SIGNATURE_TOO_LONG);
        }

        return registry.addOperation(name, generation, nodeUri, signature, updatingKey, signingKey, validFromT,
                previousDigest);
    }

    @Override
    public OperationStatusInfo getStatus(UUID operationId) {
        log.info("getStatus(): operationId = {}", LogUtil.format(operationId));

        Operation operation = registry.getOperation(operationId);
        if (operation == null) {
            log.info("No such operation");

            OperationStatusInfo info = new OperationStatusInfo();
            info.setErrorCode("unknown");
            return info;
        }
        return operation.toOperationStatusInfo();
    }

    @Override
    public RegisteredNameInfo getCurrent(String name, int generation) {
        log.info("getCurrent(): name = {}, generation = {}", LogUtil.format(name), generation);

        RegisteredName registeredName = registry.get(name, generation);
        if (registeredName == null) {
            log.info("Name/generation is not found, returning null");
            return null;
        }
        Integer latestGeneration = registry.getLatestGenerationNumber(name);
        return getRegisteredNameInfo(registeredName, latestGeneration == generation);
    }

    @Override
    public RegisteredNameInfo getCurrentForLatest(String name) {
        log.info("getCurrentForLatest(): name = {}", LogUtil.format(name));

        RegisteredName registeredName = registry.getLatestGeneration(name);
        if (registeredName == null) {
            log.info("Name is not found, returning null");
            return null;
        }
        return getRegisteredNameInfo(registeredName, true);
    }

    private RegisteredNameInfo getRegisteredNameInfo(RegisteredName registeredName, boolean latest) {
        RegisteredNameInfo info = new RegisteredNameInfo();
        info.setName(registeredName.getNameGeneration().getName());
        info.setGeneration(registeredName.getNameGeneration().getGeneration());
        info.setLatest(latest);
        info.setUpdatingKey(registeredName.getUpdatingKey());
        info.setNodeUri(registeredName.getNodeUri());
        info.setDeadline(registeredName.getDeadline().toInstant().getEpochSecond());
        SigningKey latestKey = registry.getLatestKey(registeredName.getNameGeneration());
        if (latestKey != null) {
            info.setSigningKey(latestKey.getSigningKey());
            info.setValidFrom(latestKey.getValidFrom().toInstant().getEpochSecond());
        }
        info.setDigest(registeredName.getDigest());
        return info;
    }

    @Override
    public boolean isFree(String name) {
        log.info("isFree(): name = {}", LogUtil.format(name));

        RegisteredName registeredName = registry.getLatestGeneration(name);
        return registeredName == null || registeredName.getDeadline().before(Util.now());
    }

}
