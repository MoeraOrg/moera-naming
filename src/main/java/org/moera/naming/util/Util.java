package org.moera.naming.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

public class Util {

    public static Timestamp now() {
        return Timestamp.from(Instant.now());
    }

    public static String base64encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base64decode(String s) {
        return Base64.getDecoder().decode(s);
    }

}
