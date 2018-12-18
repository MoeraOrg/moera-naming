package org.moera.naming.rpc;

import java.util.regex.Pattern;

public class Rules {

    public static final int NAME_MAX_LENGTH = 127;
    public static final Pattern NAME_PATTERN = Pattern.compile("^[^./:\\s]+$");
    public static final int NODE_URI_MAX_LENGTH = 255;

}
