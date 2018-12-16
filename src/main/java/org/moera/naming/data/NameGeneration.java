package org.moera.naming.data;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

@Embeddable
public class NameGeneration implements Serializable {

    private static final long serialVersionUID = -6236816230823929126L;

    @NotNull
    @Size(max=127)
    private String name;

    @NotNull
    private int generation;

    public NameGeneration() {
        this("", 0);
    }

    public NameGeneration(String name) {
        this(name, 0);
    }

    public NameGeneration(String name, int generation) {
        this.name = name;
        this.generation = generation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

}
