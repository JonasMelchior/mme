package com.github.jonasmelchior.mymme.protocol.s11;

import com.github.jonasmelchior.mymme.protocol.s11.codec.GtpDecoder;
import com.github.jonasmelchior.mymme.protocol.s11.codec.GtpEncoder;
import com.github.jonasmelchior.mymme.protocol.s11.model.*;
import org.junit.jupiter.api.Test;
import java.net.InetAddress;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class GtpCodecTest {

    @Test
    public void testEncodeDecodeCreateSessionRequest() throws Exception {
        Gtpv2Header header = new Gtpv2Header(Gtpv2Constants.TYPE_CREATE_SESSION_REQUEST, 0, 12345);
        Gtpv2Message csr = new Gtpv2Message(header);

        csr.addElement(new ImsiIe("123456789012345", (byte)0));
        csr.addElement(new RatTypeIe((byte)6, (byte)0));
        csr.addElement(new ServingNetworkIe("001", "01", (byte)0));
        csr.addElement(new FullyQualifiedTeidIe((byte)10, 100, InetAddress.getByName("127.0.0.1"), (byte)0));
        csr.addElement(new ApnIe("internet", (byte)0));
        csr.addElement(new UliIe("001", "01", 1, 1, (byte)0));

        BearerContextIe bearerContext = new BearerContextIe((byte)0);
        bearerContext.addElement(new EpsBearerIdIe((byte)5, (byte)0));
        bearerContext.addElement(new BearerQosIe((byte)9, (byte)15, false, false, (byte)0));
        csr.addElement(bearerContext);

        byte[] encoded = GtpEncoder.encode(csr);
        assertNotNull(encoded);
        assertTrue(encoded.length > 30);

        Gtpv2Message decoded = GtpDecoder.decode(encoded);
        assertEquals(Gtpv2Constants.TYPE_CREATE_SESSION_REQUEST, decoded.getHeader().getMessageType());
        assertEquals(0, decoded.getHeader().getTeid());
        assertEquals(12345, decoded.getHeader().getSequenceNumber());

        Optional<ImsiIe> imsi = decoded.getElement(ImsiIe.class, 0);
        assertTrue(imsi.isPresent());
        assertEquals("123456789012345", imsi.get().getImsi());

        Optional<ApnIe> apn = decoded.getElement(ApnIe.class, 0);
        assertTrue(apn.isPresent());
        assertEquals("internet", apn.get().getApn());

        Optional<BearerContextIe> bc = decoded.getElement(BearerContextIe.class, 0);
        assertTrue(bc.isPresent());
        assertEquals(2, bc.get().getElements().size());
    }

    @Test
    public void testDecodeCreateSessionResponse() throws Exception {
        // Mock a CSR Response: Header (Type=33, TEID=100, Seq=12345) + Cause(16) + F-TEID(SGW S11) + Bearer Context
        Gtpv2Header header = new Gtpv2Header(Gtpv2Constants.TYPE_CREATE_SESSION_RESPONSE, 100, 12345);
        Gtpv2Message response = new Gtpv2Message(header);
        response.addElement(new CauseIe((byte)16, (byte)0)); // Request Accepted
        response.addElement(new FullyQualifiedTeidIe((byte)11, 200, InetAddress.getByName("127.0.0.2"), (byte)0)); // SGW S11 GTP-C
        
        BearerContextIe bc = new BearerContextIe((byte)0);
        bc.addElement(new EpsBearerIdIe((byte)5, (byte)0));
        bc.addElement(new FullyQualifiedTeidIe((byte)0, 300, InetAddress.getByName("127.0.0.3"), (byte)0)); // SGW S1-U
        response.addElement(bc);

        byte[] encoded = GtpEncoder.encode(response);
        Gtpv2Message decoded = GtpDecoder.decode(encoded);

        assertEquals(Gtpv2Constants.TYPE_CREATE_SESSION_RESPONSE, decoded.getHeader().getMessageType());
        assertEquals(100, decoded.getHeader().getTeid());
        
        Optional<CauseIe> cause = decoded.getElement(CauseIe.class, 0);
        assertTrue(cause.isPresent());
        assertEquals(16, cause.get().getCauseValue());

        Optional<FullyQualifiedTeidIe> sgwS11 = decoded.getElement(FullyQualifiedTeidIe.class, 0);
        assertTrue(sgwS11.isPresent());
        assertEquals(200, sgwS11.get().getTeid());
    }
}
