package org.moera.naming.rpc;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.inject.Inject;

import org.moera.commons.util.LogUtil;
import org.moera.naming.Config;
import org.moera.naming.data.Operation;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.SigningKey;
import org.moera.naming.registry.Registry;
import org.moera.naming.rpc.exception.ServiceError;
import org.moera.naming.rpc.exception.ServiceException;
import org.moera.naming.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class NamingService {

    private static final Logger log = LoggerFactory.getLogger(NamingService.class);

    @Inject
    private Config config;

    @Inject
    private Registry registry;

    @JsonRpcMethod
    public UUID put(
            String name,
            int generation,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Long validFrom,
            byte[] previousDigest,
            byte[] signature
    ) {
        log.info("put(): name = {}, generation = {}, updatingKey = {}, nodeUri = {},"
                + " signingKey = {}, validFrom = {}, previousDigest = {},"
                + " signature = {}",
                LogUtil.format(name), generation, LogUtil.format(updatingKey), LogUtil.format(nodeUri),
                LogUtil.format(signingKey), LogUtil.formatTimestamp(validFrom), LogUtil.format(previousDigest),
                LogUtil.format(signature));

        if (ObjectUtils.isEmpty(name)) {
            throw new ServiceException(ServiceError.NAME_EMPTY);
        }
        if (name.length() > Rules.NAME_MAX_LENGTH) {
            throw new ServiceException(ServiceError.NAME_TOO_LONG);
        }
        if (!Rules.isNameValid(name)) {
            throw new ServiceException(ServiceError.NAME_FORBIDDEN_CHARS);
        }
        if (!config.isGenerationSupported(generation)) {
            throw new ServiceException(ServiceError.GENERATION_RESERVED);
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
        Timestamp validFromT = Util.toTimestamp(validFrom);
        if (previousDigest != null && previousDigest.length != Rules.DIGEST_LENGTH) {
            throw new ServiceException(ServiceError.PREVIOUS_DIGEST_WRONG_LENGTH);
        }
        if (signature != null && signature.length > Rules.SIGNATURE_MAX_LENGTH) {
            throw new ServiceException(ServiceError.SIGNATURE_TOO_LONG);
        }

        return registry.addOperation(name, generation, nodeUri, signature, updatingKey, signingKey, validFromT,
                previousDigest);
    }

    @JsonRpcMethod
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

    @JsonRpcMethod
    public RegisteredNameInfo getCurrent(String name, int generation) {
        log.info("getCurrent(): name = {}, generation = {}", LogUtil.format(name), generation);

        return getRegisteredNameInfo(name, generation, null);
    }

    @JsonRpcMethod
    public RegisteredNameInfo getPast(String name, int generation, long at) {
        log.info("getPast(): name = {}, generation = {}, at = {}", LogUtil.format(name), generation, at);

        return getRegisteredNameInfo(name, generation, Util.toTimestamp(at));
    }

    @JsonRpcMethod
    public RegisteredNameInfo getSimilar(String name) {
        log.info("getSimilar(): name = {}", LogUtil.format(name));

        return getRegisteredNameInfo(registry.getSimilar(name), null);
    }

    @JsonRpcMethod
    public List<SigningKeyInfo> getAllKeys(String name, int generation) {
        log.info("getAllKeys(): name = {}, generation = {}", LogUtil.format(name), generation);

        return registry.getAllKeys(name, generation).stream()
                .map(this::getSigningKeyInfo)
                .collect(Collectors.toList());
    }

    @JsonRpcMethod
    public List<RegisteredNameInfo> getAll(long at, int page, int size) {
        log.info("getAll(): at = {}, page = {}, size = {}", at, page, size);

        return registry.getAll(Util.toTimestamp(at), page, size).stream()
                .map(this::getRegisteredNameMinimalInfo)
                .collect(Collectors.toList());
    }

    @JsonRpcMethod
    public List<RegisteredNameInfo> getAllNewer(long at, int page, int size) {
        log.info("getAllNewer(): at = {}, page = {}, size = {}", at, page, size);

        return registry.getAllNewer(Util.toTimestamp(at), page, size).stream()
                .map(this::getRegisteredNameMinimalInfo)
                .collect(Collectors.toList());
    }

    private RegisteredNameInfo getRegisteredNameInfo(String name, int generation, Timestamp at) {
        RegisteredName registeredName = registry.get(name, generation);
        if (registeredName == null) {
            log.info("Name/generation is not found, returning null");
            return null;
        }
        return getRegisteredNameInfo(registeredName, at);
    }

    private RegisteredNameInfo getRegisteredNameInfo(RegisteredName registeredName, Timestamp at) {
        if (registeredName == null) {
            return null;
        }

        RegisteredNameInfo info = new RegisteredNameInfo();
        info.setName(registeredName.getNameGeneration().getName());
        info.setGeneration(registeredName.getNameGeneration().getGeneration());
        info.setUpdatingKey(registeredName.getUpdatingKey());
        info.setNodeUri(registeredName.getNodeUri());
        info.setCreated(Util.toEpochSecond(registeredName.getCreated()));
        SigningKey key = at == null
                ? registry.getLatestKey(info.getName(), info.getGeneration())
                : registry.getKeyValidAt(info.getName(), info.getGeneration(), at);
        if (key != null) {
            info.setSigningKey(key.getSigningKey());
            info.setValidFrom(Util.toEpochSecond(key.getValidFrom()));
        }
        info.setDigest(registeredName.getDigest());
        return info;
    }

    private RegisteredNameInfo getRegisteredNameMinimalInfo(RegisteredName registeredName) {
        if (registeredName == null) {
            return null;
        }

        RegisteredNameInfo info = new RegisteredNameInfo();
        info.setName(registeredName.getNameGeneration().getName());
        info.setGeneration(registeredName.getNameGeneration().getGeneration());
        info.setNodeUri(registeredName.getNodeUri());
        info.setCreated(Util.toEpochSecond(registeredName.getCreated()));
        return info;
    }

    private SigningKeyInfo getSigningKeyInfo(SigningKey signingKey) {
        if (signingKey == null) {
            return null;
        }

        SigningKeyInfo info = new SigningKeyInfo();
        info.setKey(signingKey.getSigningKey());
        info.setValidFrom(Util.toEpochSecond(signingKey.getValidFrom()));
        return info;
    }

    @JsonRpcMethod
    public boolean isFree(String name, int generation) {
        log.info("isFree(): name = {}, generation = {}", LogUtil.format(name), LogUtil.format(generation));

        return registry.get(name, generation) == null;
    }

}
