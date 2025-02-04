package org.moera.naming.data;

import java.sql.Timestamp;
import java.util.UUID;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.moera.lib.naming.types.OperationStatus;
import org.moera.lib.naming.types.OperationStatusInfo;
import org.moera.naming.rpc.exception.ServiceError;
import org.moera.naming.util.Util;

@Entity
@Table(name = "operations")
public class Operation {

    @Id
    @Access(AccessType.PROPERTY)
    private UUID id;

    @NotNull
    @Size(max = 127)
    private String name;

    @Size(max = 255)
    private String nodeUri;

    private byte[] signature;

    private byte[] updatingKey;

    private byte[] signingKey;

    private Timestamp validFrom;

    private byte[] previousDigest;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private OperationStatus status = OperationStatus.ADDED;

    @NotNull
    private Timestamp added = Util.now();

    private Timestamp completed;

    private String errorCode;

    @NotNull
    private int generation;

    public Operation() {
    }

    public Operation(
            String name,
            int generation,
            String nodeUri,
            byte[] signature,
            byte[] updatingKey,
            byte[] signingKey,
            Timestamp validFrom,
            byte[] previousDigest) {

        id = UUID.randomUUID();
        this.name = name;
        this.generation = generation;
        this.nodeUri = nodeUri;
        this.signature = signature;
        this.updatingKey = updatingKey;
        this.signingKey = signingKey;
        this.validFrom = validFrom;
        this.previousDigest = previousDigest;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeUri() {
        return nodeUri;
    }

    public void setNodeUri(String nodeUri) {
        this.nodeUri = nodeUri;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getUpdatingKey() {
        return updatingKey;
    }

    public void setUpdatingKey(byte[] updatingKey) {
        this.updatingKey = updatingKey;
    }

    public byte[] getSigningKey() {
        return signingKey;
    }

    public void setSigningKey(byte[] signingKey) {
        this.signingKey = signingKey;
    }

    public Timestamp getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Timestamp validFrom) {
        this.validFrom = validFrom;
    }

    public byte[] getPreviousDigest() {
        return previousDigest;
    }

    public void setPreviousDigest(byte[] previousDigest) {
        this.previousDigest = previousDigest;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public Timestamp getAdded() {
        return added;
    }

    public void setAdded(Timestamp added) {
        this.added = added;
    }

    public Timestamp getCompleted() {
        return completed;
    }

    public void setCompleted(Timestamp completed) {
        this.completed = completed;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public OperationStatusInfo toOperationStatusInfo() {
        OperationStatusInfo info = new OperationStatusInfo();
        info.setOperationId(getId());
        info.setName(getName());
        info.setGeneration(getGeneration());
        info.setStatus(getStatus());
        info.setAdded(Util.toEpochSecond(getAdded()));
        info.setCompleted(Util.toEpochSecond(getCompleted()));
        info.setErrorCode(getErrorCode());
        ServiceError serviceError = ServiceError.forCode(getErrorCode());
        if (serviceError != null) {
            info.setErrorMessage(serviceError.getMessage());
        }
        return info;
    }

}
