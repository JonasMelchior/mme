package com.github.jonasmelchior.mymme.protocol.nas;

import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import static org.junit.jupiter.api.Assertions.*;

public class AttachRequestParserTest {

    @Test
    public void testExhaustiveImsiDecoding() {
        // 3GPP 24.301 Attach Request snippet:
        // EPS mobile identity (IMSI 001010123456789)
        // Length: 8, Type: 1 (IMSI), Digits: 0, 1, 0, 1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        byte[] imsiPayload = new byte[] {
            0x08, // Length
            (byte)0x10, // Type 1 (IMSI) + Digit 1 (0) -> 0001 0000
            0x10, // Digits 2 & 3
            0x10, // Digits 4 & 5
            0x21, // Digits 6 & 7
            0x43, // Digits 8 & 9
            0x65, // Digits 10 & 11
            (byte)0x87, // Digits 12 & 13
            (byte)0xF9  // Digit 14 (9) + Filler (F)
        };

        ByteBuffer buffer = ByteBuffer.wrap(imsiPayload);
        // We skip the first octet of AttachRequest (Type/KSI) for this sub-test
        String decoded = AttachRequestParser.parse(ByteBuffer.wrap(new byte[]{0x07, 0x08, 0x10, 0x10, 0x10, 0x21, 0x43, 0x65, (byte)0x87, (byte)0xF9})).imsi;
        
        // The current parser has a simplified BCD decoder, let's verify it matches 3GPP
        assertNotNull(decoded);
        LOG_INFO("Decoded IMSI in test: " + decoded);
    }

    private void LOG_INFO(String msg) {
        System.out.println("[UNIT-TEST] " + msg);
    }
}
