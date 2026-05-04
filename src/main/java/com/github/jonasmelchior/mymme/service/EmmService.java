package com.github.jonasmelchior.mymme.service;

import com.github.jonasmelchior.mymme.data.UeContext;
import com.github.jonasmelchior.mymme.repository.UeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.SocketAddress;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmmService {

    private static final Logger LOG = Logger.getLogger(EmmService.class);

    @jakarta.inject.Inject
    UeRepository ueRepository;

    @jakarta.inject.Inject
    EmmStateMachine stateMachine;

    @jakarta.inject.Inject
    com.github.jonasmelchior.mymme.protocol.s6a.S6aService s6aService;

    @jakarta.inject.Inject
    com.github.jonasmelchior.mymme.protocol.s11.GtpService gtpService;

    @jakarta.inject.Inject
    com.github.jonasmelchior.mymme.protocol.s1ap.S1apService s1apService;

    private static final java.util.concurrent.atomic.AtomicLong MME_UE_S1AP_ID_GEN = new java.util.concurrent.atomic.AtomicLong(1);

    public void processAttachRequest(int enbUeS1apId, java.net.SocketAddress enbAddress, com.github.jonasmelchior.mymme.protocol.nas.AttachRequestParser.AttachRequestData data) {
        String imsi = data.imsi;
        LOG.infof("Processing EMM Attach Request for IMSI: %s", imsi);

        UeContext context = ueRepository.findByImsi(imsi).orElse(new UeContext());
        context.setImsi(imsi);
        context.setEnbUeS1apId(enbUeS1apId);
        context.setEnbAddress(enbAddress);
        context.setMmeUeS1apId(MME_UE_S1AP_ID_GEN.getAndIncrement()); 
        context.setUeSecurityCapabilities(data.ueSecurityCapabilities);
        context.setCurrentProcedure(UeContext.ProcedureType.ATTACH);
        
        // Location from S1AP
        context.setMcc(data.mcc);
        context.setMnc(data.mnc);
        context.setTac(data.tac);
        context.setCellId(data.cellId);

        // Parse ESM container if present
        if (data.esmMessageContainer != null) {
            com.github.jonasmelchior.mymme.protocol.nas.PdnConnectivityRequestParser.PdnConnectivityData pdnData = 
                com.github.jonasmelchior.mymme.protocol.nas.PdnConnectivityRequestParser.parse(data.esmMessageContainer);
            if (pdnData != null && pdnData.apn != null) {
                LOG.infof("EMM: Extracted APN from Attach Request: %s", pdnData.apn);
                context.setApn(pdnData.apn);
            }
        }

        ueRepository.save(context);

        stateMachine.onEvent(context, EmmStateMachine.EmmEvent.ATTACH_REQUEST_RECEIVED);
        
        // Trigger S6a Authentication Information Request (AIR)
        s6aService.sendAuthenticationInformationRequest(imsi);
    }

    public void onAuthenticationVectorsReceived(@jakarta.enterprise.event.Observes com.github.jonasmelchior.mymme.service.AuthenticationVectorsReceivedEvent event) {
        String imsi = event.getImsi();
        LOG.infof("Authentication vectors received for IMSI: %s. Sending Authentication Request.", imsi);
        
        ueRepository.findByImsi(imsi).ifPresent(context -> {
            context.setRand(event.getRand());
            context.setAutn(event.getAutn());
            context.setXres(event.getXres());
            
            byte[] kAsme = event.getkAsme();
            LOG.infof("EMM: Received kAsme length: %d", kAsme != null ? kAsme.length : 0);
            context.setkAsme(kAsme);
            ueRepository.save(context);

            LOG.infof("EMM: Sending Auth Req with RAND=%s, AUTN=%s", bytesToHex(event.getRand()), bytesToHex(event.getAutn()));

            byte[] nasAuthRequest = com.github.jonasmelchior.mymme.protocol.nas.NasEncoder.encodeAuthenticationRequest(
                (byte)0, event.getRand(), event.getAutn()
            );

            s1apService.sendDownlinkNasTransport(
                (int)context.getMmeUeS1apId(), 
                context.getEnbUeS1apId(), 
                context.getEnbAddress(), 
                nasAuthRequest
            );
        });
    }

    public void processAuthenticationResponse(int enbUeS1apId, byte[] res) {
        LOG.infof("EMM: Processing Authentication Response for ENB_UE_S1AP_ID: %d. Length: %d", enbUeS1apId, res.length);
        ueRepository.findByEnbUeS1apId(enbUeS1apId).ifPresent(context -> {
            byte[] xres = context.getXres();
            LOG.infof("EMM: Comparing RES (hex): %s with XRES (hex): %s", bytesToHex(res), bytesToHex(xres));
            
            if (java.util.Arrays.equals(res, xres)) {
                LOG.info("EMM: Authentication successful (RES matches XRES). Sending Security Mode Command.");
                
                byte integrityAlg = (byte) 2; // EIA2 (AES-CMAC)
                byte cipheringAlg = (byte) 0; // EEA0 (Null)
                
                byte[] kAsme = context.getkAsme();
                LOG.infof("EMM: Using kAsme(hex, len=%d): %s", kAsme.length, bytesToHex(kAsme));

                // Derive NAS Keys (using first 16 bytes of KDF output)
                byte[] kNasInt = com.github.jonasmelchior.mymme.protocol.nas.CryptoUtils.deriveNasKey(kAsme, (byte) 0x02, integrityAlg);
                byte[] kNasEnc = com.github.jonasmelchior.mymme.protocol.nas.CryptoUtils.deriveNasKey(kAsme, (byte) 0x01, cipheringAlg);
                
                // Also derive using last 16 bytes for logging/debugging
                byte[] kNasIntLast16 = deriveNasKeyLast16(kAsme, (byte) 0x02, integrityAlg);
                LOG.infof("EMM: Derived kNasInt(bits 0-127): %s", bytesToHex(kNasInt));
                LOG.infof("EMM: Derived kNasInt(bits 128-255): %s", bytesToHex(kNasIntLast16));

                context.setkNasInt(kNasInt);
                context.setkNasEnc(kNasEnc);
                
                // Trigger Security Mode Command
                byte[] nasSecurityModeCommand = com.github.jonasmelchior.mymme.protocol.nas.NasEncoder.encodeSecurityModeCommand(
                    integrityAlg, cipheringAlg, (byte)0, context.getUeSecurityCapabilities(), kNasInt, context.getDlNasCount()
                );
                
                // Increment DL NAS Count after sending
                context.setDlNasCount(context.getDlNasCount() + 1);
                ueRepository.save(context);

                s1apService.sendDownlinkNasTransport(
                    (int)context.getMmeUeS1apId(), 
                    context.getEnbUeS1apId(), 
                    context.getEnbAddress(), 
                    nasSecurityModeCommand
                );
            } else {
                LOG.errorf("EMM: Authentication failed (RES mismatch for IMSI: %s). Sending Authentication Reject.", context.getImsi());
                
                byte[] authReject = com.github.jonasmelchior.mymme.protocol.nas.NasEncoder.encodeAuthenticationReject();
                s1apService.sendDownlinkNasTransport(
                    (int)context.getMmeUeS1apId(), 
                    context.getEnbUeS1apId(), 
                    context.getEnbAddress(), 
                    authReject
                );
            }
        });
    }

    private byte[] deriveNasKeyLast16(byte[] kAsme, byte algType, byte algId) {
        byte[] s = new byte[7];
        s[0] = 0x15; // FC
        s[1] = algType; // P0
        s[2] = 0x00; // L0 (hi)
        s[3] = 0x01; // L0 (lo)
        s[4] = algId; // P1
        s[5] = 0x00; // L1 (hi)
        s[6] = 0x01; // L1 (lo)
        
        byte[] derived = com.github.jonasmelchior.mymme.protocol.nas.CryptoUtils.kdf(kAsme, s);
        return java.util.Arrays.copyOfRange(derived, 16, 32);
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @jakarta.inject.Inject
    jakarta.enterprise.event.Event<AuthenticationSuccessEvent> authSuccessEvent;

    public void processSecurityModeComplete(int enbUeS1apId) {
        LOG.infof("EMM: Processing Security Mode Complete for ENB_UE_S1AP_ID: %d", enbUeS1apId);
        ueRepository.findByEnbUeS1apId(enbUeS1apId).ifPresent(context -> {
            LOG.info("EMM: Security Mode Complete received. Proceeding to Session Creation.");
            authSuccessEvent.fire(new AuthenticationSuccessEvent(context.getImsi()));
        });
    }

    public void onAuthenticationSuccess(@jakarta.enterprise.event.Observes AuthenticationSuccessEvent event) {
        String imsi = event.getImsi();
        LOG.infof("Authentication successful for IMSI: %s. Proceeding to S6a Update Location.", imsi);
        ueRepository.findByImsi(imsi).ifPresent(context -> {
            stateMachine.onEvent(context, EmmStateMachine.EmmEvent.AUTHENTICATION_SUCCESS);
            
            // Calculate ULR-Flags
            // Bit 1: S6a-indicator (always 1)
            // Bit 5: Initial-Attach-Indicator (1 for ATTACH)
            int ulrFlags = 2; // Default S6a-indicator
            if (context.getCurrentProcedure() == UeContext.ProcedureType.ATTACH) {
                ulrFlags |= 32; // Initial-Attach-Indicator
            }
            
            s6aService.sendUpdateLocationRequest(imsi, context.getRatType(), ulrFlags);
        });
    }

    public void onUpdateLocationReceived(@jakarta.enterprise.event.Observes UpdateLocationReceivedEvent event) {
        String imsi = event.getImsi();
        LOG.infof("Update Location received for IMSI: %s. Subscription Data: APN=%s, QCI=%d. Proceeding to S11 Session Creation.", imsi, event.getApn(), event.getQci());
        ueRepository.findByImsi(imsi).ifPresent(context -> {
            context.setApn(event.getApn());
            // We could also store QCI in UeContext if needed
            ueRepository.save(context);
            gtpService.sendCreateSessionRequest(imsi);
        });
    }

    public void onSessionCreated(String imsi) {
        LOG.infof("S11 Session created for IMSI: %s. Finalizing Attach procedure...", imsi);
        ueRepository.findByImsi(imsi).ifPresent(context -> {
            stateMachine.onEvent(context, EmmStateMachine.EmmEvent.ATTACH_ACCEPT_SENT);

            // 1. Send S1AP Initial Context Setup Request using real SGW info from S11
            s1apService.sendInitialContextSetupRequest(
                (int)context.getMmeUeS1apId(), 
                context.getEnbUeS1apId(), 
                context.getEnbAddress(), 
                context.getSgwIp(), 
                context.getSgwS1Uteid()
            );

            // 2. Generate and Send NAS Attach Accept
            byte[] nasAttachAccept = com.github.jonasmelchior.mymme.protocol.nas.NasEncoder.encodeAttachAccept(
                "001", "01", 1, 1, 1, (int)System.currentTimeMillis()
            );
            s1apService.sendDownlinkNasTransport(
                (int)context.getMmeUeS1apId(), 
                context.getEnbUeS1apId(), 
                context.getEnbAddress(), 
                nasAttachAccept
            );
        });
    }
}
