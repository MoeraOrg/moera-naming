package org.moera.naming.util;

import java.util.UUID;

public class LogUtil {

    public static String format(String value) {
        return value != null ? String.format("'%s'", value) : "null";
    }

    public static String format(UUID value) {
        return value != null ? String.format("'%s'", value.toString()) : "null";
    }

    public static String format(byte[] value) {
        return value != null ? Util.dump(value) : "null";
    }

    public static String formatTimestamp(Long value) {
        return value != null ? Util.formatTimestamp(value) : "null";
    }

}
