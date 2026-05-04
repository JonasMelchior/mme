package com.github.jonasmelchior.mymme.protocol.s6a;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class S6aService {

    private static final Logger LOG = Logger.getLogger(S6aService.class);

    @Inject
    S6aClient s6aClient;

    public void sendAuthenticationInformationRequest(String imsi) {
        LOG.infof("S6aService: Triggering AIR for IMSI %s via S6aClient", imsi);
        s6aClient.sendAuthenticationInformationRequest(imsi);
    }

    public void sendUpdateLocationRequest(String imsi, int ratType, int ulrFlags) {
        LOG.infof("S6aService: Triggering ULR for IMSI %s (RAT: %d, Flags: %d) via S6aClient", imsi, ratType, ulrFlags);
        s6aClient.sendUpdateLocationRequest(imsi, ratType, ulrFlags);
    }
}
