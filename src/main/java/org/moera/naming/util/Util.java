package org.moera.naming.util;

import java.sql.Timestamp;
import java.time.Instant;

import org.moera.naming.rpc.Rules;

public class Util {

    public static final byte[] EMPTY_DIGEST = new byte[Rules.DIGEST_LENGTH];

    public static Timestamp now() {
        return Timestamp.from(Instant.now());
    }

    public static Timestamp toTimestamp(Long epochSecond) {
        return epochSecond != null ? Timestamp.from(Instant.ofEpochSecond(epochSecond)) : null;
    }

    public static int random(int min, int max) {
        return (int) (Math.random() * (max - min)) + min;
    }

}
