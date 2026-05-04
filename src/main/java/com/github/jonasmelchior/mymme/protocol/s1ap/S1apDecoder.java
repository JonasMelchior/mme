package com.github.jonasmelchior.mymme.protocol.s1ap;

import io.netty.channel.sctp.SctpMessage;
import org.jboss.logging.Logger;

/**
 * Stateless utility for low-level S1AP decoding.
 * High-level procedure handling is in S1apService.
 */
public class S1apDecoder {
    private static final Logger LOG = Logger.getLogger(S1apDecoder.class);

    public static void decode(SctpMessage msg) {
        byte[] data = new byte[msg.content().readableBytes()];
        msg.content().readBytes(data);
        
        if (data.length < 3) return;

        S1apLibrary.S1apMessage s1apMsg = new S1apLibrary.S1apMessage();
        int result = S1apLibrary.INSTANCE.s1ap_decode_pdu(s1apMsg, data, data.length);
        
        if (result == 0) {
            LOG.infof("Decoded S1AP PDU: ProcedureCode=%d", s1apMsg.procedureCode);
        } else {
            LOG.error("Failed to decode S1AP PDU using native library");
        }
    }
}
