package org.moera.naming.data;

import java.sql.Timestamp;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.moera.naming.rpc.Rules;
import org.moera.naming.util.Util;

@Entity
@Table(name = "registered_names")
public class RegisteredName {

    @EmbeddedId
    private NameGeneration nameGeneration = new NameGeneration();

    @NotNull
    private byte[] updatingKey;

    @NotNull
    private Timestamp created = Util.now();

    @NotNull
    @Size(max = Rules.NODE_URI_MAX_LENGTH)
    private String nodeUri = "";

    @NotNull
    private byte[] digest;

    public RegisteredName() {
    }

    public RegisteredName(String name, int generation) {
        nameGeneration = new NameGeneration(name, generation);
    }

    public NameGeneration getNameGeneration() {
        return nameGeneration;
    }

    public void setNameGeneration(NameGeneration nameGeneration) {
        this.nameGeneration = nameGeneration;
    }

    public byte[] getUpdatingKey() {
        return updatingKey;
    }

    public void setUpdatingKey(byte[] updatingKey) {
        this.updatingKey = updatingKey;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public String getNodeUri() {
        return nodeUri;
    }

    public void setNodeUri(String nodeUri) {
        this.nodeUri = nodeUri;
    }

    public byte[] getDigest() {
        return digest;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

}
