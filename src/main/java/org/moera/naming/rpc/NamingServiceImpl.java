package org.moera.naming.rpc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.inject.Inject;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import org.moera.commons.util.Util;
import org.moera.naming.data.Operation;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.SigningKey;
import org.moera.naming.registry.Registry;
import org.moera.naming.rpc.exception.ServiceError;
import org.moera.naming.rpc.exception.ServiceException;
import org.moera.naming.util.LogUtil;
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
            boolean newGeneration,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Long validFrom,
            byte[] signature) {

        log.info("put(): name = {}, newGeneration = {}, updatingKey = {}, nodeUri = {},"
                + " signingKey = {}, validFrom = {}, signature = {}",
                LogUtil.format(name), newGeneration, LogUtil.format(updatingKey), LogUtil.format(nodeUri),
                LogUtil.format(signingKey), LogUtil.formatTimestamp(validFrom), LogUtil.format(signature));

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

        return registry.addOperation(name, newGeneration, nodeUri, signature, updatingKey, signingKey, validFromT);
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
            log.info("Name/generation is not found");
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
            log.info("Name is not found");
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
        SigningKey latestKey = registry.getLatestKey(registeredName.getNameGeneration());
        if (latestKey != null) {
            info.setSigningKey(Util.base64encode(latestKey.getSigningKey()));
            info.setValidFrom(latestKey.getValidFrom().getTime());
        }
        return info;
    }

}
