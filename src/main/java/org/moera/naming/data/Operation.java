package org.moera.naming.data;

import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.moera.commons.util.Util;
import org.moera.naming.rpc.OperationStatus;
import org.moera.naming.rpc.OperationStatusInfo;

@Entity
@Table(name = "operations")
public class Operation {

    @Id
    @Access(AccessType.PROPERTY)
    private UUID id;

    @NotNull
    @Size(max = 127)
    private String name;

    @NotNull
    private boolean newGeneration;

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

    private Integer generation;

    public Operation() {
    }

    public Operation(
            String name,
            boolean newGeneration,
            String nodeUri,
            byte[] signature,
            byte[] updatingKey,
            byte[] signingKey,
            Timestamp validFrom,
            byte[] previousDigest) {

        id = UUID.randomUUID();
        this.name = name;
        this.newGeneration = newGeneration;
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

    public boolean isNewGeneration() {
        return newGeneration;
    }

    public void setNewGeneration(boolean newGeneration) {
        this.newGeneration = newGeneration;
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

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
    }

    public OperationStatusInfo toOperationStatusInfo() {
        OperationStatusInfo info = new OperationStatusInfo();
        info.setOperationId(getId());
        info.setStatus(getStatus());
        info.setAdded(getAdded());
        info.setCompleted(getCompleted());
        info.setErrorCode(getErrorCode());
        info.setGeneration(getGeneration());
        return info;
    }

}
