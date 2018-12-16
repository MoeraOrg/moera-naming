package org.moera.naming.data.exception;

public class NameEmptyException extends RuntimeException {

    public NameEmptyException() {
        super("name is empty");
    }

}
