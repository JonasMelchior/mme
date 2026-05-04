package com.github.jonasmelchior.mymme.protocol.nas;

import java.nio.ByteBuffer;
import java.util.Arrays;
import static com.github.jonasmelchior.mymme.protocol.nas.NasConstants.*;

public class NasEncoder {

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(NasEncoder.class);

    public static byte[] encodeAuthenticationRequest(byte ksi, byte[] rand, byte[] autn) {
        if (rand.length != 16) {
            throw new IllegalArgumentException("RAND must be exactly 16 bytes. Current length: " + rand.length);
        }
        if (autn.length != 16) {
            throw new IllegalArgumentException("AUTN must be exactly 16 bytes. Current length: " + autn.length);
        }

        // Authentication Request structure (35 bytes total):
        // [0] SecHeader(4)|PD(4) = 0x07 (Plain EMM)
        // [1] Type = 0x52
        // [2] KSI/Spare = 0x00
        // [3-18] RAND (16 bytes)
        // [19] AUTN Length (1 byte, 0x10)
        // [20-35] AUTN (16 bytes)
        ByteBuffer buffer = ByteBuffer.allocate(36);
        buffer.put((byte) ((SECURITY_PLAIN << 4) | (PD_EMM & 0x0F))); 
        buffer.put(TYPE_AUTHENTICATION_REQUEST);
        buffer.put((byte) ((0 << 4) | (ksi & 0x07))); 
        buffer.put(rand);
        buffer.put((byte) autn.length);
        buffer.put(autn);
        
        byte[] packet = buffer.array();
        LOG.infof("Sending Authentication Request (Hex): %s", bytesToHex(packet));
        return packet;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static byte[] encodeSecurityModeCommand(byte integrityAlg, byte cipheringAlg, byte ksi, byte[] replayedUeCaps, byte[] kNasInt, int dlNasCount) {
        // Security Mode Command (Plain NAS message part):
        // [0] PD(4)|SecHeader(4) = 0x07 (Plain EMM)
        // [1] MsgType = 0x5d
        // [2] Selected NAS Security Algs (Ciphering << 4 | Integrity) - TS 24.301 9.9.3.23
        // [3] NAS Key Set Identifier (KSI)
        // [4...] Replayed UE Security Capabilities (LV)

        int ueCapsLen = (replayedUeCaps != null) ? replayedUeCaps.length : 0;
        ByteBuffer plainBuffer = ByteBuffer.allocate(1 + 1 + 1 + 1 + 1 + ueCapsLen);
        plainBuffer.put((byte) ((SECURITY_PLAIN << 4) | (PD_EMM & 0x0F))); 
        plainBuffer.put(TYPE_SECURITY_MODE_COMMAND);
        plainBuffer.put((byte) (((cipheringAlg & 0x07) << 4) | (integrityAlg & 0x07)));
        plainBuffer.put((byte) (ksi & 0x07));
        
        // Replayed UE Caps (LV)
        plainBuffer.put((byte) ueCapsLen);
        if (replayedUeCaps != null) {
            plainBuffer.put(replayedUeCaps);
        }

        byte[] plainNas = plainBuffer.array();
        
        // Wrap in Security Header (Type 4: Integrity Protected with new EPS security context)
        // [0] SecHeader(4)|PD(4)
        // [1-4] MAC (4 bytes)
        // [5] Sequence Number (1 byte)
        // [6...] Plain NAS message
        byte sn = (byte) (dlNasCount & 0xFF);
        byte secHeaderTypeAndPd = (byte) ((SECURITY_INTEGRITY_NEW << 4) | (PD_EMM & 0x0F));
        LOG.infof("SMC: Using kNasInt(hex): %s", bytesToHex(kNasInt));
        
        // Calculate MAC
        // TS 24.301 4.4.4.2: MAC for SMC covers [SecHeader|PD] + [SN] + [Plain NAS]
        // However, srsRAN (and some others) only include [SN] + [Plain NAS] in their 
        // implementation for the initial SMC.
        ByteBuffer macInputMsg = ByteBuffer.allocate(1 + plainNas.length);
        macInputMsg.put(sn);
        macInputMsg.put(plainNas);
        
        byte[] niaInput = CryptoUtils.formatNiaInput(0, (byte) 0, (byte) 1, macInputMsg.array());
        LOG.infof("SMC: niaInput(hex): %s", bytesToHex(niaInput));
        byte[] mac;
        if (integrityAlg == 2) { // 128-EIA2 (AES-CMAC)
            byte[] fullMac = CryptoUtils.calculateAesCmac(kNasInt, niaInput);
            mac = Arrays.copyOfRange(fullMac, 0, 4); // Truncate to 32 bits
        } else {
            LOG.warnf("Integrity algorithm %d not fully implemented, using placeholder MAC", integrityAlg);
            mac = new byte[4];
        }

        ByteBuffer secureBuffer = ByteBuffer.allocate(6 + plainNas.length);
        secureBuffer.put((byte) ((SECURITY_INTEGRITY_NEW << 4) | (PD_EMM & 0x0F)));
        secureBuffer.put(mac); 
        secureBuffer.put(sn); // SN
        secureBuffer.put(plainNas);

        byte[] packet = secureBuffer.array();
        LOG.infof("Sending Security Mode Command (Hex): %s", bytesToHex(packet));
        return packet;
    }

    public static byte[] encodeAttachAccept(String mcc, String mnc, int tac, int utranTac, int cellId, int mmeUeId) {
        // Attach Accept: Very simplified for now
        ByteBuffer buffer = ByteBuffer.allocate(32);
        buffer.put((byte) (PD_EMM & 0x0F));
        buffer.put(TYPE_ATTACH_ACCEPT);
        buffer.put((byte) 0x02); // EPS attach result: EPS only
        buffer.put((byte) 0x5a); // T3412 (GPRS timer)
        buffer.put((byte) 0x00); // TAI list length (simplified)
        buffer.put((byte) 0x00); // ESM message container length (simplified)
        return buffer.array();
    }

    public static byte[] encodeAuthenticationReject() {
        // Authentication Reject: Security Header Type (4) | PD (4) = 1 byte
        // Message Type (8) = 1 byte
        // EMM Cause (8) = 1 byte
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.put((byte) ((SECURITY_PLAIN << 4) | (PD_EMM & 0x0F))); 
        buffer.put(TYPE_AUTHENTICATION_REJECT);
        buffer.put((byte) 0x03); // Cause #3: Illegal UE
        return buffer.array();
    }
}
