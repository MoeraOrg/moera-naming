package org.moera.naming.data;

import java.sql.Timestamp;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

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
    private byte[] signingKey = new byte[0];

    @NotNull
    private Timestamp validFrom = Util.now();

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

}
