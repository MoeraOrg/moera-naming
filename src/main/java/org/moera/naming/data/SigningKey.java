package org.moera.naming.data;

import java.sql.Timestamp;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import org.moera.naming.util.Util;

@Entity
@Table(name = "signing_keys")
public class SigningKey {

    @Id
    @GeneratedValue
    @Access(AccessType.PROPERTY)
    private long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "generation"),
            @JoinColumn(name = "name")
    })
    private RegisteredName registeredName;

    @NotNull
    private byte[] signingKey;

    @NotNull
    private Timestamp validFrom;

    @NotNull
    private Timestamp created = Util.now();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RegisteredName getRegisteredName() {
        return registeredName;
    }

    public void setRegisteredName(RegisteredName registeredName) {
        this.registeredName = registeredName;
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

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

}
