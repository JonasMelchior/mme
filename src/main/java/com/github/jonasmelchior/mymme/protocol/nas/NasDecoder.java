package com.github.jonasmelchior.mymme.protocol.nas;

import java.nio.ByteBuffer;
import java.net.SocketAddress;
import org.jboss.logging.Logger;
import static com.github.jonasmelchior.mymme.protocol.nas.NasConstants.*;

public class NasDecoder {
    private static final Logger LOG = Logger.getLogger(NasDecoder.class);

    public static void decode(byte[] data, int enbUeS1apId, java.net.SocketAddress enbAddress, 
                              String mcc, String mnc, int tac, int cellId, int ratType,
                              NasService nasService) {
        if (data == null || data.length < 1) {
            LOG.warn("Empty NAS message received");
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte firstOctet = buffer.get();
        int securityHeaderType = (firstOctet >> 4) & 0x0F;
        int protocolDiscriminator = firstOctet & 0x0F;

        if (securityHeaderType != SECURITY_PLAIN) {
            if (buffer.remaining() < 5) return;
            buffer.position(buffer.position() + 5); 
            if (!buffer.hasRemaining()) return;
            byte innerFirstOctet = buffer.get();
            protocolDiscriminator = innerFirstOctet & 0x0F;
        }

        if (protocolDiscriminator == PD_EMM) {
            handleEmmMessage(buffer, enbUeS1apId, enbAddress, mcc, mnc, tac, cellId, ratType, nasService);
        } else if (protocolDiscriminator == PD_ESM) {
            handleEsmMessage(buffer, enbUeS1apId, nasService);
        }
    }

    private static void handleEmmMessage(ByteBuffer buffer, int enbUeS1apId, java.net.SocketAddress enbAddress, 
                                         String mcc, String mnc, int tac, int cellId, int ratType,
                                         NasService nasService) {
        if (!buffer.hasRemaining()) return;
        byte messageType = buffer.get();
        
        switch (messageType) {
            case TYPE_ATTACH_REQUEST: 
                AttachRequestParser.AttachRequestData data = AttachRequestParser.parse(buffer);
                data.mcc = mcc;
                data.mnc = mnc;
                data.tac = tac;
                data.cellId = cellId;
                data.ratType = ratType;
                nasService.handleAttachRequest(enbUeS1apId, enbAddress, data); 
                break;
            case TYPE_AUTHENTICATION_RESPONSE: 
                byte[] res = AuthenticationResponseParser.parse(buffer);
                nasService.handleAuthenticationResponse(enbUeS1apId, res);
                break;
            case TYPE_SECURITY_MODE_COMMAND: nasService.handleSecurityModeCommand(); break;
            case TYPE_SECURITY_MODE_COMPLETE: nasService.handleSecurityModeComplete(enbUeS1apId); break;
            case TYPE_SECURITY_MODE_REJECT: nasService.handleSecurityModeReject(); break;
            case TYPE_ATTACH_ACCEPT: nasService.handleAttachAccept(); break;
            case TYPE_ATTACH_COMPLETE: nasService.handleAttachComplete(); break;
            case TYPE_ATTACH_REJECT: nasService.handleAttachReject(); break;
            case TYPE_DETACH_REQUEST: nasService.handleDetachRequest(); break;
            case TYPE_DETACH_ACCEPT: nasService.handleDetachAccept(); break;
            case TYPE_TRACKING_AREA_UPDATE_REQUEST: nasService.handleTauRequest(); break;
            default:
                LOG.warnf("NAS: Unsupported EMM Message Type 0x%02X", messageType);
        }
    }

    private static void handleEsmMessage(ByteBuffer buffer, int enbUeS1apId, NasService nasService) {
        if (buffer.remaining() < 2) return;
        buffer.get(); 
        buffer.get(); 
        byte messageType = buffer.get();
        
        switch (messageType) {
            case TYPE_PDN_CONNECTIVITY_REQUEST: nasService.handlePdnConnectivityRequest(); break;
            case TYPE_ACTIVATE_DEFAULT_EPS_BEARER_CONTEXT_REQUEST: nasService.handleActivateDefaultBearerRequest(); break;
            default:
                LOG.warnf("NAS: Unsupported ESM Message Type 0x%02X", messageType);
        }
    }
}
