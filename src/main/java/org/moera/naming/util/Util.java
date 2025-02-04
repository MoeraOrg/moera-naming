package org.moera.naming.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

import org.moera.lib.Rules;

public class Util {

    public static final byte[] EMPTY_DIGEST = new byte[Rules.DIGEST_LENGTH];

    public static Timestamp now() {
        return Timestamp.from(Instant.now());
    }

    public static Long toEpochSecond(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant().getEpochSecond() : null;
    }

    public static Timestamp toTimestamp(Long epochSecond) {
        return epochSecond != null ? Timestamp.from(Instant.ofEpochSecond(epochSecond)) : null;
    }

    public static byte[] base64decode(String s) {
        return Base64.getDecoder().decode(s);
    }

    public static int random(int min, int max) {
        return (int) (Math.random() * (max - min)) + min;
    }

}
