package org.moera.naming.rpc;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import javax.inject.Inject;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import org.moera.naming.data.NameGeneration;
import org.moera.naming.data.RegisteredName;
import org.moera.naming.data.SigningKey;
import org.moera.naming.data.Storage;
import org.moera.naming.rpc.exception.ServiceError;
import org.moera.naming.rpc.exception.ServiceException;
import org.moera.naming.util.SignatureDataBuilder;
import org.moera.naming.util.Util;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@AutoJsonRpcServiceImpl
public class NamingServiceImpl implements NamingService {

    @Inject
    private Storage storage;

    @Override
    public long put(
            String name,
            boolean newGeneration,
            String updatingKey,
            String nodeUri,
            String signingKey,
            Long validFrom,
            String signature) {

        if (StringUtils.isEmpty(name)) {
            throw new ServiceException(ServiceError.NAME_EMPTY);
        }
        if (name.length() > Rules.NAME_MAX_LENGTH) {
            throw new ServiceException(ServiceError.NAME_TOO_LONG);
        }
        if (!Rules.NAME_PATTERN.matcher(name).matches()) {
            throw new ServiceException(ServiceError.NAME_FORBIDDEN_CHARS);
        }
        byte[] updatingKeyD = null;
        if (!StringUtils.isEmpty(updatingKey)) {
            try {
                updatingKeyD = Util.base64decode(updatingKey);
            } catch (IllegalArgumentException e) {
                throw new ServiceException(ServiceError.UPDATING_KEY_INVALID_ENCODING);
            }
        }
        if (nodeUri != null && nodeUri.length() > Rules.NODE_URI_MAX_LENGTH) {
            throw new ServiceException(ServiceError.NODE_URI_TOO_LONG);
        }
        byte[] signingKeyD = null;
        if (!StringUtils.isEmpty(signingKey)) {
            try {
                signingKeyD = Util.base64decode(signingKey);
            } catch (IllegalArgumentException e) {
                throw new ServiceException(ServiceError.SIGNING_KEY_INVALID_ENCODING);
            }
        }
        Timestamp now = Util.now();
        Timestamp validFromT = validFrom != null ? Timestamp.from(Instant.ofEpochSecond(validFrom)) : now;
        if (validFromT.before(now)) {
            throw new ServiceException(ServiceError.VALID_FROM_IN_PAST);
        }

        RegisteredName latest = storage.getLatestGeneration(name);
        if (isForceNewGeneration(latest)) {
            RegisteredName target = newGeneration(latest, name);
            putNew(target, name, updatingKeyD, nodeUri, signingKeyD, validFromT);
        } else {
            if (newGeneration) {
                RegisteredName target = newGeneration(latest, name);
                validateSignature(target, null, updatingKeyD, nodeUri, signingKeyD, validFromT, signature);
                putNew(target, name, updatingKeyD, nodeUri, signingKeyD, validFromT);
            } else {
                SigningKey latestKey = storage.getLatestKey(latest.getNameGeneration());
                validateSignature(latest, latestKey, updatingKeyD, nodeUri, signingKeyD, validFromT, signature);
                putExisting(latest, updatingKeyD, nodeUri, signingKeyD, validFromT, signature);
            }
        }

        return 0;
    }

    private void putNew(
            RegisteredName target,
            String name,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Timestamp validFrom) {

        if (!StringUtils.isEmpty(nodeUri)) {
            target.setNodeUri(nodeUri);
        }
        if (updatingKey != null) { // FIXME null is error here
            target.setUpdatingKey(updatingKey);
        }
        SigningKey targetKey = null;
        if (signingKey != null) {
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
            Timestamp validFrom,
            String signature) {

        storage.save(target);
    }

    private void validateSignature(
            RegisteredName target,
            SigningKey latestKey,
            byte[] updatingKey,
            String nodeUri,
            byte[] signingKey,
            Timestamp validFrom,
            String signature) {

        SignatureDataBuilder buf = new SignatureDataBuilder();
        try {
            buf.append(target.getNameGeneration().getName());
            buf.append(updatingKey != null ? updatingKey : target.getUpdatingKey());
            buf.append(nodeUri != null ? nodeUri : target.getNodeUri());
            if (signingKey != null) {
                buf.append(signingKey);
                buf.append(validFrom.getTime());
            } else if (latestKey != null) {
                buf.append(latestKey.getSigningKey());
                buf.append(latestKey.getValidFrom().getTime());
            }
            System.out.println(Util.dump(buf.toBytes()));
        } catch (IOException e) {
            throw new ServiceException(ServiceError.IO_EXCEPTION);
        }
        // TODO And then convert buf to byte[] and verify signature
        // throw new ServiceException(ServiceError.SIGNATURE_INVALID);
    }

    private boolean isForceNewGeneration(RegisteredName latest) {
        if (latest == null) {
            return true;
        }
        return latest.getDeadline().before(Util.now());
    }

    private RegisteredName newGeneration(RegisteredName latest, String name) {
        int generation = latest == null ? 0 : latest.getNameGeneration().getGeneration() + 1;
        RegisteredName target = new RegisteredName();
        target.setNameGeneration(new NameGeneration(name, generation));
        return target;
    }

}
