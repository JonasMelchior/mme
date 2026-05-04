package com.github.jonasmelchior.mymme.protocol.nas;

import java.nio.ByteBuffer;

public class AuthenticationResponseParser {
    public static byte[] parse(ByteBuffer buffer) {
        // Authentication Response parameter (RES) is LV
        int length = buffer.get() & 0xFF;
        byte[] res = new byte[length];
        buffer.get(res);
        return res;
    }
}
