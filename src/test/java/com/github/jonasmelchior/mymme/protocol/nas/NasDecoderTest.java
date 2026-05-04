package com.github.jonasmelchior.mymme.protocol.nas;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class NasDecoderTest {

    @InjectMock
    NasService nasService;

    @Test
    public void testDecodeAttachRequest() {
        // Example NAS Attach Request hex
        // PD=7 (EMM), MsgType=0x41 (Attach Request)
        // KSI=0, AttachType=1
        // Identity Length=5, Identity=0x29, 0x26, 0x01, 0x01, 0x01 (IMSI 2620111...)
        byte[] attachRequest = new byte[] { 0x07, 0x41, 0x01, 0x05, 0x29, 0x26, 0x01, 0x01, 0x01 };
        
        assertDoesNotThrow(() -> NasDecoder.decode(attachRequest, 1, null, nasService));
    }
    
    @Test
    public void testDecodeInvalidPd() {
        byte[] invalidPd = new byte[] { 0x01, 0x41 };
        assertDoesNotThrow(() -> NasDecoder.decode(invalidPd, 1, null, nasService));
    }
}
