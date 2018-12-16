package org.moera.naming.util;

import java.sql.Timestamp;
import java.time.Instant;

public class Util {

    public static Timestamp now() {
        return Timestamp.from(Instant.now());
    }

}
