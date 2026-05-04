package com.github.jonasmelchior.mymme.protocol.nas;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Cipher;

public class CryptoUtils {

    /**
     * KDF defined in TS 33.401 Annex A.
     */
    public static byte[] kdf(byte[] key, byte[] s) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
            sha256HMAC.init(secretKey);
            return sha256HMAC.doFinal(s);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("KDF failed", e);
        }
    }

    /**
     * Derive NAS keys (K_nas_enc or K_nas_int) from K_ASME.
     * algType: 0x01 for ENC, 0x02 for INT
     * algId: 0x00 for EEA0/EIA0, 0x01 for EIA1/EEA1, etc.
     */
    public static byte[] deriveNasKey(byte[] kAsme, byte algType, byte algId) {
        byte[] s = new byte[7];
        s[0] = 0x15; // FC
        s[1] = algType; // P0
        s[2] = 0x00; // L0 (hi)
        s[3] = 0x01; // L0 (lo)
        s[4] = algId; // P1
        s[5] = 0x00; // L1 (hi)
        s[6] = 0x01; // L1 (lo)
        
        byte[] derived = kdf(kAsme, s);
        // NAS keys are the 128 least significant bits (bits 128-255) of the KDF output
        return Arrays.copyOfRange(derived, 16, 32);
    }

    /**
     * AES-CMAC (128-EIA2) integrity calculation.
     * RFC 4493
     */
    public static byte[] calculateAesCmac(byte[] key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));

            // Subkey generation
            byte[] zero = new byte[16];
            byte[] l = cipher.doFinal(zero);
            byte[] k1 = generateSubkey(l);
            byte[] k2 = generateSubkey(k1);

            int n = (data.length + 15) / 16;
            boolean flag = (n > 0 && data.length % 16 == 0);
            if (n == 0) {
                n = 1;
                flag = false;
            }

            byte[] mLast;
            if (flag) {
                mLast = xor(Arrays.copyOfRange(data, (n - 1) * 16, n * 16), k1);
            } else {
                byte[] padded = new byte[16];
                int lastBlockLen = data.length % 16;
                System.arraycopy(data, (n - 1) * 16, padded, 0, lastBlockLen);
                padded[lastBlockLen] = (byte) 0x80;
                mLast = xor(padded, k2);
            }

            byte[] x = new byte[16];
            byte[] y = new byte[16];
            for (int i = 0; i < n - 1; i++) {
                y = xor(x, Arrays.copyOfRange(data, i * 16, (i + 1) * 16));
                x = cipher.doFinal(y);
            }
            y = xor(x, mLast);
            return cipher.doFinal(y);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("AES-CMAC failed", e);
        }
    }

    private static byte[] generateSubkey(byte[] l) {
        byte[] k = new byte[16];
        int msb = (l[0] & 0x80) != 0 ? 1 : 0;
        
        // Shift left by 1
        for (int i = 0; i < 15; i++) {
            k[i] = (byte) ((l[i] << 1) | ((l[i + 1] & 0x80) != 0 ? 1 : 0));
        }
        k[15] = (byte) (l[15] << 1);

        if (msb == 1) {
            k[15] ^= 0x87;
        }
        return k;
    }

    private static byte[] xor(byte[] a, byte[] b) {
        byte[] res = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = (byte) (a[i] ^ b[i]);
        }
        return res;
    }

    /**
     * Format input for NIA algorithms.
     * TS 33.401: COUNT || BEARER || DIRECTION || 0...0 || MESSAGE
     */
    public static byte[] formatNiaInput(int count, byte bearer, byte direction, byte[] message) {
        // Many implementations use 8 bytes of header: COUNT(4) + BEARER(5bit)|DIR(1bit)|00(2bit) + 3 bytes zero
        ByteBuffer buffer = ByteBuffer.allocate(8 + message.length);
        buffer.putInt(count);
        buffer.put((byte) ((bearer & 0x1F) << 3 | (direction & 0x01) << 2));
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        buffer.put(message);
        return buffer.array();
    }
}
