package com.github.jonasmelchior.mymme.protocol.nas;

import jakarta.enterprise.context.ApplicationScoped;
import java.net.SocketAddress;
import org.jboss.logging.Logger;
import static com.github.jonasmelchior.mymme.protocol.nas.NasConstants.*;

@ApplicationScoped
public class NasService {
    private static final Logger LOG = Logger.getLogger(NasService.class);

    @jakarta.inject.Inject
    com.github.jonasmelchior.mymme.service.EmmService emmService;

    // --- Exhaustive EMM Handlers ---
    public void handleAttachRequest(int enbUeS1apId, java.net.SocketAddress enbAddress, AttachRequestParser.AttachRequestData data) { 
        LOG.infof("EMM: Handling Attach Request for IMSI: %s, ENB_UE_S1AP_ID: %d", data.imsi, enbUeS1apId); 
        // Delegate to EmmService for exhaustive business logic
        emmService.processAttachRequest(enbUeS1apId, enbAddress, data); 
    }
    
    public void handleAttachAccept() { LOG.info("EMM: Handling Attach Accept"); }
    public void handleAttachComplete() { LOG.info("EMM: Handling Attach Complete"); }
    public void handleAttachReject() { LOG.info("EMM: Handling Attach Reject"); }
    public void handleDetachRequest() { LOG.info("EMM: Handling Detach Request"); }
    public void handleDetachAccept() { LOG.info("EMM: Handling Detach Accept"); }
    public void handleTauRequest() { LOG.info("EMM: Handling TAU Request"); }
    public void handleTauAccept() { LOG.info("EMM: Handling TAU Accept"); }
    public void handleTauComplete() { LOG.info("EMM: Handling TAU Complete"); }
    public void handleTauReject() { LOG.info("EMM: Handling TAU Reject"); }
    public void handleAuthenticationRequest() { LOG.info("EMM: Handling Authentication Request"); }
    public void handleAuthenticationResponse(int enbUeS1apId, byte[] res) { 
        LOG.infof("EMM: Handling Authentication Response for ENB_UE_S1AP_ID: %d", enbUeS1apId);
        emmService.processAuthenticationResponse(enbUeS1apId, res);
    }
    public void handleAuthenticationFailure() { LOG.info("EMM: Handling Authentication Failure"); }
    public void handleIdentityRequest() { LOG.info("EMM: Handling Identity Request"); }
    public void handleIdentityResponse() { LOG.info("EMM: Handling Identity Response"); }
    public void handleSecurityModeCommand() { LOG.info("EMM: Handling Security Mode Command"); }
    public void handleSecurityModeComplete(int enbUeS1apId) { 
        LOG.infof("EMM: Handling Security Mode Complete for ENB_UE_S1AP_ID: %d", enbUeS1apId);
        emmService.processSecurityModeComplete(enbUeS1apId);
    }
    public void handleSecurityModeReject() { LOG.info("EMM: Handling Security Mode Reject"); }
    public void handleEmmStatus() { LOG.info("EMM: Handling EMM Status"); }
    public void handleEmmInformation() { LOG.info("EMM: Handling EMM Information"); }
    public void handleDownlinkNasTransport() { LOG.info("EMM: Handling Downlink NAS Transport"); }
    public void handleUplinkNasTransport() { LOG.info("EMM: Handling Uplink NAS Transport"); }

    // --- Exhaustive ESM Handlers ---
    public void handlePdnConnectivityRequest() { LOG.info("ESM: Handling PDN Connectivity Request"); }
    public void handlePdnConnectivityReject() { LOG.info("ESM: Handling PDN Connectivity Reject"); }
    public void handlePdnDisconnectRequest() { LOG.info("ESM: Handling PDN Disconnect Request"); }
    public void handlePdnDisconnectReject() { LOG.info("ESM: Handling PDN Disconnect Reject"); }
    public void handleActivateDefaultBearerRequest() { LOG.info("ESM: Handling Activate Default Bearer Request"); }
    public void handleActivateDefaultBearerAccept() { LOG.info("ESM: Handling Activate Default Bearer Accept"); }
    public void handleActivateDefaultBearerReject() { LOG.info("ESM: Handling Activate Default Bearer Reject"); }
    public void handleEsmInformationRequest() { LOG.info("ESM: Handling ESM Information Request"); }
    public void handleEsmInformationResponse() { LOG.info("ESM: Handling ESM Information Response"); }
    public void handleEsmStatus() { LOG.info("ESM: Handling ESM Status"); }
}
