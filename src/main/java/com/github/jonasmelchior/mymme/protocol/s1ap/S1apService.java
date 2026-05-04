package com.github.jonasmelchior.mymme.protocol.s1ap;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import java.net.SocketAddress;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import static com.github.jonasmelchior.mymme.protocol.s1ap.S1apProcedureCodes.*;

@ApplicationScoped
public class S1apService {
    private static final Logger LOG = Logger.getLogger(S1apService.class);

    @jakarta.inject.Inject
    com.github.jonasmelchior.mymme.protocol.nas.NasService nasService;

    @jakarta.inject.Inject
    com.github.jonasmelchior.mymme.repository.UeRepository ueRepository;

    @jakarta.inject.Inject
    EnbManager enbManager;

    public void handleIncomingMessage(io.netty.channel.ChannelHandlerContext ctx, byte[] data) {
        S1apLibrary.S1apMessage msg = new S1apLibrary.S1apMessage();
        int result = S1apLibrary.INSTANCE.s1ap_decode_pdu(msg, data, data.length);

        if (result != 0) {
            LOG.error("Failed to decode S1AP PDU");
            return;
        }

        LOG.infof("S1AP Dispatcher: ProcedureCode=%d, Direction=%d", msg.procedureCode, msg.direction);

        switch ((int) msg.procedureCode) {
            case ID_S1_SETUP: handleS1Setup(ctx, msg); break;
            case ID_INITIAL_UE_MESSAGE: handleInitialUeMessage(ctx, msg.msg.s1ap_InitialUEMessageIEs); break;
            case ID_UPLINK_NAS_TRANSPORT: handleUplinkNasTransport(msg); break;
            case ID_UE_CONTEXT_RELEASE_REQUEST: handleUeContextReleaseRequest(msg); break;
            case ID_UE_CONTEXT_RELEASE: handleUeContextRelease(msg); break;
            case ID_UE_CAPABILITY_INFO_INDICATION: handleUeCapabilityInfoIndication(msg); break;
            case ID_NAS_NON_DELIVERY_INDICATION: handleNasNonDeliveryIndication(msg); break;
            case ID_ERROR_INDICATION: handleErrorIndication(msg); break;
            case ID_RESET: handleReset(msg); break;
            case ID_HANDOVER_PREPARATION: handleHandoverPreparation(msg); break;
            case ID_HANDOVER_RESOURCE_ALLOCATION: handleHandoverResourceAllocation(msg); break;
            case ID_HANDOVER_NOTIFICATION: handleHandoverNotification(msg); break;
            case ID_PATH_SWITCH_REQUEST: handlePathSwitchRequest(msg); break;
            case ID_HANDOVER_CANCEL: handleHandoverCancel(msg); break;
            case ID_E_RAB_SETUP: handleERabSetup(msg); break;
            case ID_E_RAB_MODIFY: handleERabModify(msg); break;
            case ID_E_RAB_RELEASE: handleERabRelease(msg); break;
            case ID_E_RAB_RELEASE_INDICATION: handleERabReleaseIndication(msg); break;
            case ID_INITIAL_CONTEXT_SETUP: handleInitialContextSetup(msg); break;
            case ID_PAGING: handlePaging(msg); break;
            case ID_DOWNLINK_NAS_TRANSPORT: handleDownlinkNasTransport(msg); break;
            case ID_ENB_CONFIGURATION_UPDATE: handleEnbConfigurationUpdate(msg); break;
            case ID_MME_CONFIGURATION_UPDATE: handleMmeConfigurationUpdate(msg); break;
            case ID_LOCATION_REPORTING_CONTROL: handleLocationReportingControl(msg); break;
            case ID_LOCATION_REPORTING_FAILURE_INDICATION: handleLocationReportingFailureIndication(msg); break;
            case ID_LOCATION_REPORT: handleLocationReport(msg); break;
            case ID_OVERLOAD_START: handleOverloadStart(msg); break;
            case ID_OVERLOAD_STOP: handleOverloadStop(msg); break;
            case ID_WRITE_REPLACE_WARNING: handleWriteReplaceWarning(msg); break;
            case ID_ENB_DIRECT_INFORMATION_TRANSFER: handleEnbDirectInformationTransfer(msg); break;
            case ID_MME_DIRECT_INFORMATION_TRANSFER: handleMmeDirectInformationTransfer(msg); break;
            case ID_ENB_CONFIGURATION_TRANSFER: handleEnbConfigurationTransfer(msg); break;
            case ID_MME_CONFIGURATION_TRANSFER: handleMmeConfigurationTransfer(msg); break;
            case ID_CELL_TRAFFIC_TRACE: handleCellTrafficTrace(msg); break;
            case ID_KILL: handleKill(msg); break;
            case ID_UERADIO_CAPABILITY_MATCH: handleUeRadioCapabilityMatch(msg); break;
            default:
                LOG.warnf("S1AP: Unsupported Procedure Code %d", msg.procedureCode);
        }
    }

    private void handleS1Setup(io.netty.channel.ChannelHandlerContext ctx, S1apLibrary.S1apMessage msg) { 
        S1apLibrary.S1SetupRequestIEs ies = msg.msg.s1ap_S1SetupRequestIEs;
        String enbName = "Unknown";
        if (ies.eNBname.buf != null) {
            enbName = new String(ies.eNBname.getBytes());
        }
        
        byte[] plmn = ies.global_ENB_ID.pLMNidentity.getBytes();
        LOG.infof("Handling S1 Setup Request from eNodeB: %s, PLMN: %02x%02x%02x", 
            enbName, plmn[0], plmn[1], plmn[2]);
            
        sendS1SetupResponse(ctx);
    }

    private void sendS1SetupResponse(io.netty.channel.ChannelHandlerContext ctx) {
        com.sun.jna.Pointer[] bufferPtr = new com.sun.jna.Pointer[1];
        com.sun.jna.Memory lengthPtr = new com.sun.jna.Memory(4);

        int res = S1apLibrary.INSTANCE.s1ap_mme_generate_s1_setup_response(
            bufferPtr, lengthPtr, 
            "VertexMME", 
            (byte)0, (byte)0, (byte)1, 
            (byte)0, (byte)1, (byte)0, 
            (short)1, (byte)1, (byte)255
        );

        if (res >= 0) {
            int length = lengthPtr.getInt(0);
            LOG.infof("S1 Setup Response generated, length=%d bytes. Sending...", length);
            byte[] responseData = bufferPtr[0].getByteArray(0, length);
            ctx.writeAndFlush(new io.netty.channel.sctp.SctpMessage(0, 0, io.netty.buffer.Unpooled.copiedBuffer(responseData)))
               .addListener(future -> {
                   if (future.isSuccess()) {
                       LOG.info("S1 Setup Response sent successfully to Netty.");
                   } else {
                       LOG.error("Failed to send S1 Setup Response: ", future.cause());
                   }
               });
        } else {
            LOG.errorf("Failed to generate S1 Setup Response, result code=%d", res);
        }
    }

    public void sendInitialContextSetupRequest(int mmeUeId, int enbUeId, java.net.SocketAddress enbAddress, byte[] sgwIp, int teid) {
        io.netty.channel.Channel enbChannel = enbManager.getEnbChannel(enbAddress);
        if (enbChannel == null) {
            LOG.errorf("Cannot send Initial Context Setup Request: eNodeB %s not connected", enbAddress);
            return;
        }

        LOG.info("Exhaustive: Sending Initial Context Setup Request...");
        com.sun.jna.Pointer[] bufferPtr = new com.sun.jna.Pointer[1];
        com.sun.jna.Memory lengthPtr = new com.sun.jna.Memory(4);
        int res = S1apLibrary.INSTANCE.s1ap_mme_generate_initial_context_setup_request(bufferPtr, lengthPtr, mmeUeId, enbUeId, sgwIp, teid);
        if (res >= 0) {
            int length = lengthPtr.getInt(0);
            byte[] responseData = bufferPtr[0].getByteArray(0, length);
            enbChannel.writeAndFlush(new io.netty.channel.sctp.SctpMessage(0, 0, io.netty.buffer.Unpooled.copiedBuffer(responseData)));
        }
    }

    public void sendDownlinkNasTransport(int mmeUeId, int enbUeId, java.net.SocketAddress enbAddress, byte[] nasPdu) {
        io.netty.channel.Channel enbChannel = enbManager.getEnbChannel(enbAddress);
        if (enbChannel == null) {
            LOG.errorf("Cannot send Downlink NAS Transport: eNodeB %s not connected", enbAddress);
            return;
        }

        LOG.info("Exhaustive: Sending Downlink NAS Transport...");
        com.sun.jna.Pointer[] bufferPtr = new com.sun.jna.Pointer[1];
        com.sun.jna.Memory lengthPtr = new com.sun.jna.Memory(4);
        int res = S1apLibrary.INSTANCE.s1ap_mme_generate_downlink_nas_transport(bufferPtr, lengthPtr, mmeUeId, enbUeId, nasPdu, nasPdu.length);
        if (res >= 0) {
            int length = lengthPtr.getInt(0);
            byte[] responseData = bufferPtr[0].getByteArray(0, length);
            enbChannel.writeAndFlush(new io.netty.channel.sctp.SctpMessage(0, 0, io.netty.buffer.Unpooled.copiedBuffer(responseData)));
        }
    }

    private void handleInitialUeMessage(io.netty.channel.ChannelHandlerContext ctx, S1apLibrary.InitialUEMessageIEs ies) { 
        LOG.infof("Initial UE Message from eNodeB. ENB_UE_S1AP_ID=%d", ies.eNB_UE_S1AP_ID);
        byte[] nasPdu = ies.nas_pdu.getBytes();
        
        String mcc = null;
        String mnc = null;
        int tac = 0;
        int cellId = 0;
        int ratType = 1004; // Default to WB-EUTRAN
        
        if (ies.tai != null && ies.tai.pLMNidentity != null) {
            String[] plmn = decodePlmn(ies.tai.pLMNidentity.getBytes());
            mcc = plmn[0];
            mnc = plmn[1];
            if (ies.tai.tAC != null) {
                tac = (int) com.sun.jna.Pointer.nativeValue(ies.tai.tAC.getPointer());
            }
        }
        
        if (ies.eutran_cgi != null && ies.eutran_cgi.cell_ID != null) {
            cellId = (int) com.sun.jna.Pointer.nativeValue(ies.eutran_cgi.cell_ID.getPointer());
            
            // Enterprise Logic: Detect NB-IoT based on CellID patterns or S1AP parameters
            // Standard LTE is usually WB-EUTRAN (1004)
            // NB-IoT is NB-IoT (1005)
            // Some vendors use specific CellID ranges for NB-IoT
            if ((cellId & 0x0F) > 0) { // Example heuristic: if last nibble > 0, it might be NB-IoT cell
                 // ratType = 1005; 
            }
        }

        if (nasPdu.length > 0) {
            LOG.infof("Extracted NAS-PDU (%d bytes). Dispatching to NAS Decoder...", nasPdu.length);
            com.github.jonasmelchior.mymme.protocol.nas.NasDecoder.decode(
                nasPdu, (int) ies.eNB_UE_S1AP_ID, ctx.channel().remoteAddress(), 
                mcc, mnc, tac, cellId, nasService);
        }
    }

    private void handleUplinkNasTransport(S1apLibrary.S1apMessage msg) { 
        S1apLibrary.UplinkNASTransportIEs ies = msg.msg.s1ap_UplinkNASTransportIEs;
        LOG.infof("Handling Uplink NAS Transport for MME_UE_S1AP_ID=%d, ENB_UE_S1AP_ID=%d", 
                  ies.mme_ue_s1ap_id, ies.enb_ue_s1ap_id);
        
        String mcc = null;
        String mnc = null;
        int tac = 0;
        int cellId = 0;
        
        if (ies.tai != null && ies.tai.pLMNidentity != null) {
            String[] plmn = decodePlmn(ies.tai.pLMNidentity.getBytes());
            mcc = plmn[0];
            mnc = plmn[1];
        }

        byte[] nasPdu = ies.nas_pdu.getBytes();
        if (nasPdu.length > 0) {
            LOG.infof("Extracted NAS-PDU (%d bytes). Dispatching to NAS Decoder...", nasPdu.length);
            com.github.jonasmelchior.mymme.protocol.nas.NasDecoder.decode(
                nasPdu, (int) ies.enb_ue_s1ap_id, null, 
                mcc, mnc, tac, cellId, nasService
            );
        }
    }

    private String[] decodePlmn(byte[] bytes) {
        if (bytes == null || bytes.length < 3) return new String[]{"000", "00"};
        int mcc1 = bytes[0] & 0x0F;
        int mcc2 = (bytes[0] >> 4) & 0x0F;
        int mcc3 = bytes[1] & 0x0F;
        int mnc3 = (bytes[1] >> 4) & 0x0F;
        int mnc1 = bytes[2] & 0x0F;
        int mnc2 = (bytes[2] >> 4) & 0x0F;
        
        String mcc = "" + mcc1 + mcc2 + mcc3;
        String mnc = (mnc3 == 0xF) ? "" + mnc1 + mnc2 : "" + mnc1 + mnc2 + mnc3;
        return new String[]{mcc, mnc};
    }
    private void handleUeContextReleaseRequest(S1apLibrary.S1apMessage msg) { LOG.info("Handling UE Context Release Request"); }
    private void handleUeContextRelease(S1apLibrary.S1apMessage msg) { LOG.info("Handling UE Context Release"); }
    private void handleUeCapabilityInfoIndication(S1apLibrary.S1apMessage msg) { LOG.info("Handling UE Capability Info Indication"); }
    private void handleNasNonDeliveryIndication(S1apLibrary.S1apMessage msg) { LOG.info("Handling NAS Non-Delivery Indication"); }
    private void handleErrorIndication(S1apLibrary.S1apMessage msg) { LOG.info("Handling Error Indication"); }
    private void handleReset(S1apLibrary.S1apMessage msg) { LOG.info("Handling Reset"); }
    public void sendHandoverRequest(io.netty.channel.ChannelHandlerContext targetEnbCtx, int mmeUeId, int handoverType, int cause, byte[] container) {
        LOG.info("Exhaustive: Sending Handover Request to Target eNodeB...");
        com.sun.jna.Pointer[] bufferPtr = new com.sun.jna.Pointer[1];
        com.sun.jna.Memory lengthPtr = new com.sun.jna.Memory(4);
        
        int res = S1apLibrary.INSTANCE.s1ap_mme_generate_handover_request(
            bufferPtr, lengthPtr, mmeUeId, handoverType, cause, container, container.length
        );

        if (res >= 0) {
            int length = lengthPtr.getInt(0);
            byte[] responseData = bufferPtr[0].getByteArray(0, length);
            targetEnbCtx.writeAndFlush(new io.netty.channel.sctp.SctpMessage(0, 0, io.netty.buffer.Unpooled.copiedBuffer(responseData)));
        }
    }

    private void handleHandoverPreparation(S1apLibrary.S1apMessage msg) { 
        S1apLibrary.HandoverRequiredIEs ies = msg.msg.s1ap_HandoverRequiredIEs;
        LOG.infof("S1 Handover Required from Source eNB. MME_UE_S1AP_ID=%d, HandoverType=%d", ies.mme_ue_s1ap_id, ies.handoverType);
        
        ueRepository.findByS1apId(ies.mme_ue_s1ap_id).ifPresent(context -> {
            LOG.infof("UE Context found for Handover: IMSI=%s", context.getImsi());
        });
    }
    private void handleHandoverResourceAllocation(S1apLibrary.S1apMessage msg) { LOG.info("Handling Handover Resource Allocation"); }
    private void handleHandoverNotification(S1apLibrary.S1apMessage msg) { LOG.info("Handling Handover Notification"); }
    private void handlePathSwitchRequest(S1apLibrary.S1apMessage msg) { LOG.info("Handling Path Switch Request"); }
    private void handleHandoverCancel(S1apLibrary.S1apMessage msg) { LOG.info("Handling Handover Cancel"); }
    private void handleERabSetup(S1apLibrary.S1apMessage msg) { LOG.info("Handling E-RAB Setup"); }
    private void handleERabModify(S1apLibrary.S1apMessage msg) { LOG.info("Handling E-RAB Modify"); }
    private void handleERabRelease(S1apLibrary.S1apMessage msg) { LOG.info("Handling E-RAB Release"); }
    private void handleERabReleaseIndication(S1apLibrary.S1apMessage msg) { LOG.info("Handling E-RAB Release Indication"); }
    private void handleInitialContextSetup(S1apLibrary.S1apMessage msg) { LOG.info("Handling Initial Context Setup"); }
    private void handlePaging(S1apLibrary.S1apMessage msg) { LOG.info("Handling Paging"); }
    private void handleDownlinkNasTransport(S1apLibrary.S1apMessage msg) { LOG.info("Handling Downlink NAS Transport"); }
    private void handleEnbConfigurationUpdate(S1apLibrary.S1apMessage msg) { LOG.info("Handling eNB Configuration Update"); }
    private void handleMmeConfigurationUpdate(S1apLibrary.S1apMessage msg) { LOG.info("Handling MME Configuration Update"); }
    private void handleLocationReportingControl(S1apLibrary.S1apMessage msg) { LOG.info("Handling Location Reporting Control"); }
    private void handleLocationReportingFailureIndication(S1apLibrary.S1apMessage msg) { LOG.info("Handling Location Reporting Failure Indication"); }
    private void handleLocationReport(S1apLibrary.S1apMessage msg) { LOG.info("Handling Location Report"); }
    private void handleOverloadStart(S1apLibrary.S1apMessage msg) { LOG.info("Handling Overload Start"); }
    private void handleOverloadStop(S1apLibrary.S1apMessage msg) { LOG.info("Handling Overload Stop"); }
    private void handleWriteReplaceWarning(S1apLibrary.S1apMessage msg) { LOG.info("Handling Write Replace Warning"); }
    private void handleEnbDirectInformationTransfer(S1apLibrary.S1apMessage msg) { LOG.info("Handling eNB Direct Information Transfer"); }
    private void handleMmeDirectInformationTransfer(S1apLibrary.S1apMessage msg) { LOG.info("Handling MME Direct Information Transfer"); }
    private void handleEnbConfigurationTransfer(S1apLibrary.S1apMessage msg) { LOG.info("Handling eNB Configuration Transfer"); }
    private void handleMmeConfigurationTransfer(S1apLibrary.S1apMessage msg) { LOG.info("Handling MME Configuration Transfer"); }
    private void handleCellTrafficTrace(S1apLibrary.S1apMessage msg) { LOG.info("Handling Cell Traffic Trace"); }
    private void handleKill(S1apLibrary.S1apMessage msg) { LOG.info("Handling Kill"); }
    private void handleUeRadioCapabilityMatch(S1apLibrary.S1apMessage msg) { LOG.info("Handling UE Radio Capability Match"); }
}
