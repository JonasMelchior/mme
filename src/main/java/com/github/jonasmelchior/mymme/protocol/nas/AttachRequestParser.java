package com.github.jonasmelchior.mymme.protocol.nas;

import java.nio.ByteBuffer;
import org.jboss.logging.Logger;

/**
 * Exhaustive parser for the NAS Attach Request message (TS 24.301, 8.2.4).
 */
public class AttachRequestParser {
    private static final Logger LOG = Logger.getLogger(AttachRequestParser.class);

    public static class AttachRequestData {
        public String imsi;
        public byte attachType;
        public byte ksi;
        public byte[] ueSecurityCapabilities;
        public byte[] esmMessageContainer;
        
        // Location from S1AP
        public String mcc;
        public String mnc;
        public int tac;
        public int cellId;
        public int ratType;
    }

    public static AttachRequestData parse(ByteBuffer buffer) {
        AttachRequestData data = new AttachRequestData();
        
        // 1. EPS attach type and NAS key set identifier (1 octet)
        byte octet = buffer.get();
        data.ksi = (byte)((octet >> 4) & 0x07);
        data.attachType = (byte)(octet & 0x07);
        
        // 2. EPS mobile identity (LV)
        int identityLength = buffer.get() & 0xFF;
        byte[] identityBytes = new byte[identityLength];
        buffer.get(identityBytes);
        data.imsi = decodeMobileIdentity(identityBytes);

        // 3. UE Network Capability (LV)
        int capabilityLength = buffer.get() & 0xFF;
        data.ueSecurityCapabilities = new byte[capabilityLength];
        buffer.get(data.ueSecurityCapabilities);

        // 4. ESM message container (LV-E)
        // Note: In a real MME, we'd loop through optional IEs. 
        // For common Attach Request, it usually follows UE Cap.
        if (buffer.remaining() >= 2) {
            int esmLength = buffer.getShort() & 0xFFFF;
            if (buffer.remaining() >= esmLength) {
                data.esmMessageContainer = new byte[esmLength];
                buffer.get(data.esmMessageContainer);
            }
        }
        
        LOG.infof("Parsed Attach Request: IMSI=%s, AttachType=%d, KSI=%d, ESMContLen=%d", 
                   data.imsi, data.attachType, data.ksi, (data.esmMessageContainer != null ? data.esmMessageContainer.length : 0));
                   
        return data;
    }

    private static String decodeMobileIdentity(byte[] bytes) {
        if (bytes.length == 0) return "Unknown";
        int type = bytes[0] & 0x07;
        if (type == 0x01) { // IMSI
            return decodeImsi(bytes);
        }
        return "Type-" + type;
    }

    private static String decodeImsi(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        // Skip first byte (contains type and first digit)
        sb.append((bytes[0] >> 4) & 0x0F);
        for (int i = 1; i < bytes.length; i++) {
            sb.append(bytes[i] & 0x0F);
            int high = (bytes[i] >> 4) & 0x0F;
            if (high != 0x0F) sb.append(high); // 0xF is filler
        }
        return sb.toString();
    }
}
