package com.github.jonasmelchior.mymme.protocol.nas;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.jboss.logging.Logger;

public class PdnConnectivityRequestParser {
    private static final Logger LOG = Logger.getLogger(PdnConnectivityRequestParser.class);

    public static class PdnConnectivityData {
        public byte pdnType;
        public byte requestType;
        public String apn;
    }

    public static PdnConnectivityData parse(byte[] data) {
        if (data == null || data.length < 3) return null;
        ByteBuffer buffer = ByteBuffer.wrap(data);
        PdnConnectivityData result = new PdnConnectivityData();

        // 1. EPS bearer identity | Protocol discriminator (1 octet)
        buffer.get();
        // 2. Procedure transaction identity (1 octet)
        buffer.get();
        // 3. PDN connectivity request message type (1 octet)
        byte msgType = buffer.get();
        if ((msgType & 0xFF) != (NasConstants.TYPE_PDN_CONNECTIVITY_REQUEST & 0xFF)) {
            LOG.warnf("Expected PDN Connectivity Request (0xD0), got 0x%02X", msgType);
            return null;
        }

        // 4. PDN type | Request type (1 octet)
        byte octet = buffer.get();
        result.pdnType = (byte)(octet & 0x07);
        result.requestType = (byte)((octet >> 4) & 0x07);

        // Optional IEs
        while (buffer.hasRemaining()) {
            byte iei = buffer.get();
            switch (iei) {
                case 0x28: // Access point name (LV)
                    int apnLen = buffer.get() & 0xFF;
                    byte[] apnBytes = new byte[apnLen];
                    buffer.get(apnBytes);
                    result.apn = decodeApn(apnBytes);
                    break;
                default:
                    // Skip unknown optional IE (simplified)
                    LOG.debugf("Skipping optional ESM IEI: 0x%02X", iei);
                    if (buffer.hasRemaining()) {
                        int len = buffer.get() & 0xFF;
                        if (buffer.remaining() >= len) {
                            buffer.position(buffer.position() + len);
                        }
                    }
            }
        }

        return result;
    }

    private static String decodeApn(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < bytes.length) {
            int len = bytes[i++] & 0xFF;
            if (len == 0) break;
            if (sb.length() > 0) sb.append(".");
            if (i + len <= bytes.length) {
                sb.append(new String(bytes, i, len, StandardCharsets.UTF_8));
            }
            i += len;
        }
        return sb.toString();
    }
}
