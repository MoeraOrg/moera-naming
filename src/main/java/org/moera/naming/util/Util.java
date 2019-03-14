package org.moera.naming.util;

public class Util extends org.moera.commons.util.Util {

    public static int random(int min, int max) {
        return (int) (Math.random() * (max - min)) + min;
    }

}
