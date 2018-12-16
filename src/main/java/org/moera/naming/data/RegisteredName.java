package org.moera.naming.data;

import java.sql.Timestamp;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.moera.naming.util.Util;

@Entity
@Table(name = "registered_names")
public class RegisteredName {

    @EmbeddedId
    private NameGeneration nameGeneration = new NameGeneration();

    @NotNull
    private byte[] updatingKey = new byte[0];

    @NotNull
    private Timestamp created = Util.now();

    @NotNull
    private Timestamp deadline = Util.now();

    @NotNull
    @Size(max = 255)
    private String nodeUri = "";

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

    public Timestamp getDeadline() {
        return deadline;
    }

    public void setDeadline(Timestamp deadline) {
        this.deadline = deadline;
    }

    public String getNodeUri() {
        return nodeUri;
    }

    public void setNodeUri(String nodeUri) {
        this.nodeUri = nodeUri;
    }

}
