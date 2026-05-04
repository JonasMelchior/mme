package com.github.jonasmelchior.mymme.protocol.s6a;

import io.quarkiverse.diameter.DiameterConfig;
import io.quarkiverse.diameter.DiameterService;
import io.quarkiverse.diameter.DiameterServiceOptions;
import io.quarkus.logging.Log;
import org.jdiameter.api.*;
import org.jdiameter.api.app.*;
import org.jdiameter.api.s6a.ClientS6aSession;
import org.jdiameter.api.s6a.ClientS6aSessionListener;
import org.jdiameter.api.s6a.events.*;
import org.jdiameter.client.impl.parser.MessageParser;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import com.github.jonasmelchior.mymme.config.S6aConfig;
import com.github.jonasmelchior.mymme.service.AuthenticationVectorsReceivedEvent;

@DiameterServiceOptions(timeOut = 3000)
@DiameterService
public class S6aClient implements ClientS6aSessionListener {

    private static final int AIR_COMMAND_CODE = 318;
    private static final int ULR_COMMAND_CODE = 316;

    @DiameterConfig
    Stack stack;

    @Inject
    S6aConfig config;

    @Inject
    Event<AuthenticationVectorsReceivedEvent> authVectorsEvent;

    @Inject
    Event<com.github.jonasmelchior.mymme.service.UpdateLocationReceivedEvent> ulaEvent;

    public void sendAuthenticationInformationRequest(String imsi) {
        try {
            ApplicationId application = ApplicationId.createByAuthAppId(config.vendorId(), config.authAppId());
            ClientS6aSession session = stack.getSessionFactory().getNewAppSession(null, application, ClientS6aSession.class);

            JAuthenticationInformationRequest air = createAirRequest(imsi);

            Log.infof("S6a: Sending AIR for IMSI %s to %s", imsi, config.destinationHost());
            session.sendAuthenticationInformationRequest(air);
        } catch (Exception e) {
            Log.error("Failed to send S6a AIR", e);
        }
    }

    public void sendUpdateLocationRequest(String imsi, int ratType, int ulrFlags) {
        try {
            ApplicationId application = ApplicationId.createByAuthAppId(config.vendorId(), config.authAppId());
            ClientS6aSession session = stack.getSessionFactory().getNewAppSession(null, application, ClientS6aSession.class);

            JUpdateLocationRequest ulr = createUlrRequest(imsi, ratType, ulrFlags);

            Log.infof("S6a: Sending ULR for IMSI %s (RAT: %d, Flags: %d) to %s", imsi, ratType, ulrFlags, config.destinationHost());
            session.sendUpdateLocationRequest(ulr);
        } catch (Exception e) {
            Log.error("Failed to send S6a ULR", e);
        }
    }

    private JUpdateLocationRequest createUlrRequest(String imsi, int ratType, int ulrFlags) {
        final MessageParser parser = new MessageParser();
        final long vendor3gpp = 10415L;
        return new JUpdateLocationRequest() {
            @Override
            public String getDestinationHost() { return config.destinationHost(); }
            @Override
            public String getDestinationRealm() { return config.destinationRealm(); }
            @Override
            public int getCommandCode() { return ULR_COMMAND_CODE; }
            @Override
            public Message getMessage() throws InternalException {
                Message msg = parser.createEmptyMessage(ULR_COMMAND_CODE, config.authAppId());
                msg.setRequest(true);
                AvpSet avps = msg.getAvps();
                avps.addAvp(Avp.USER_NAME, imsi, false);
                
                avps.addAvp(1407, hexStringToByteArray(config.visitedPlmnId()), vendor3gpp, true, false);
                avps.addAvp(1032, ratType, vendor3gpp, true, false); 
                avps.addAvp(1405, ulrFlags, vendor3gpp, true, false); 

                avps.addAvp(Avp.DESTINATION_REALM, config.destinationRealm(), false);
                avps.addAvp(Avp.DESTINATION_HOST, config.destinationHost(), false);
                avps.addAvp(Avp.ORIGIN_REALM, config.originRealm(), false);
                avps.addAvp(Avp.ORIGIN_HOST, config.originHost(), false);
                return msg;
            }
            @Override
            public String getOriginHost() { return config.originHost(); }
            @Override
            public String getOriginRealm() { return config.originRealm(); }
        };
    }

    private JAuthenticationInformationRequest createAirRequest(String imsi) {
        final MessageParser parser = new MessageParser();
        return new JAuthenticationInformationRequest() {
            @Override
            public String getDestinationHost() { return config.destinationHost(); }
            @Override
            public String getDestinationRealm() { return config.destinationRealm(); }
            @Override
            public int getCommandCode() { return AIR_COMMAND_CODE; }
            @Override
            public Message getMessage() throws InternalException {
                Message msg = parser.createEmptyMessage(AIR_COMMAND_CODE, config.authAppId());
                msg.setRequest(true);
                msg.getAvps().addAvp(Avp.USER_NAME, imsi, false);
                msg.getAvps().addAvp(Avp.VISITED_PLMN_ID, hexStringToByteArray(config.visitedPlmnId()), true, false);
                msg.getAvps().addAvp(Avp.DESTINATION_REALM, config.destinationRealm(), false);
                msg.getAvps().addAvp(Avp.DESTINATION_HOST, config.destinationHost(), false);
                msg.getAvps().addAvp(Avp.ORIGIN_REALM, config.originRealm(), false);
                msg.getAvps().addAvp(Avp.ORIGIN_HOST, config.originHost(), false);
                return msg;
            }
            @Override
            public String getOriginHost() { return config.originHost(); }
            @Override
            public String getOriginRealm() { return config.originRealm(); }
        };
    }

    private byte[] hexStringToByteArray(String s) {
        if (s == null) return new byte[0];
        String cleaned = s.replaceAll("[^0-9a-fA-F]", "");
        int len = cleaned.length();
        if (len % 2 != 0) {
            cleaned = "0" + cleaned;
            len++;
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(cleaned.charAt(i), 16) << 4)
                                 + Character.digit(cleaned.charAt(i+1), 16));
        }
        return data;
    }

    private boolean isHexString(byte[] data) {
        if (data == null || data.length == 0) return false;
        for (byte b : data) {
            if (!((b >= '0' && b <= '9') || (b >= 'a' && b <= 'f') || (b >= 'A' && b <= 'F'))) {
                return false;
            }
        }
        return true;
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Override
    public void doAuthenticationInformationAnswerEvent(ClientS6aSession session, JAuthenticationInformationRequest request, JAuthenticationInformationAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        Log.info("S6a: AIA Received");
        try {
            Message msg = answer.getMessage();
            String imsi = "unknown";
            if (request != null && request.getMessage() != null) {
                Avp reqUserNameAvp = request.getMessage().getAvps().getAvp(Avp.USER_NAME);
                if (reqUserNameAvp != null) {
                    imsi = reqUserNameAvp.getUTF8String();
                }
            }
            
            byte[] rand = new byte[16];
            byte[] autn = new byte[16];
            byte[] xres = new byte[8];
            byte[] kAsme = new byte[32];

            long vendor3gpp = 10415L;
            Avp authInfoAvp = msg.getAvps().getAvp(1413, vendor3gpp);
            if (authInfoAvp == null) authInfoAvp = msg.getAvps().getAvp(1413);
            
            if (authInfoAvp != null) {
                AvpSet authInfoGroup = authInfoAvp.getGrouped();
                Avp eUtranVectorAvp = authInfoGroup.getAvp(1414, vendor3gpp);
                if (eUtranVectorAvp == null) eUtranVectorAvp = authInfoGroup.getAvp(1414);
                
                if (eUtranVectorAvp != null) {
                    AvpSet vectorGroup = eUtranVectorAvp.getGrouped();

                    Avp randAvp = vectorGroup.getAvp(1447, vendor3gpp);
                    if (randAvp == null) randAvp = vectorGroup.getAvp(1447);
                    if (randAvp != null) {
                        rand = randAvp.getOctetString();
                    }

                    Avp xresAvp = vectorGroup.getAvp(1448, vendor3gpp);
                    if (xresAvp == null) xresAvp = vectorGroup.getAvp(1448);
                    if (xresAvp != null) {
                        xres = xresAvp.getOctetString();
                    }

                    Avp autnAvp = vectorGroup.getAvp(1449, vendor3gpp);
                    if (autnAvp == null) autnAvp = vectorGroup.getAvp(1449);
                    if (autnAvp != null) {
                        autn = autnAvp.getOctetString();
                    }

                    Avp kasmeAvp = vectorGroup.getAvp(1450, vendor3gpp);
                    if (kasmeAvp == null) kasmeAvp = vectorGroup.getAvp(1450);
                    if (kasmeAvp != null) {
                        byte[] raw = kasmeAvp.getOctetString();
                        Log.infof("S6a: Raw kAsme AVP(hex, len=%d): %s", raw.length, bytesToHex(raw));
                        if (raw.length == 64 && isHexString(raw)) {
                            Log.info("S6a: kAsme is 64-char hex string, decoding...");
                            kAsme = hexStringToByteArray(new String(raw));
                        } else if (raw.length >= 32) {
                            Log.info("S6a: kAsme is binary >= 32 bytes, taking first 32 bytes");
                            kAsme = java.util.Arrays.copyOf(raw, 32);
                        } else {
                            kAsme = raw;
                        }
                    }

                } else {
                    Log.warn("S6a: AIA received without E-UTRAN-Vector AVP");
                }
            } else {
                Log.warn("S6a: AIA received without Authentication-Info AVP");
            }
            
            Log.infof("S6a: AIA Success for IMSI %s. Firing event.", imsi);
            authVectorsEvent.fire(new AuthenticationVectorsReceivedEvent(imsi, rand, autn, xres, kAsme));
        } catch (Exception e) {
            Log.error("Error processing S6a AIA", e);
        }
    }

    // --- Required Listener Methods ---
    @Override
    public void doUpdateLocationAnswerEvent(ClientS6aSession session, JUpdateLocationRequest request, JUpdateLocationAnswer answer) {
        Log.info("S6a: ULA Received");
        try {
            Message msg = answer.getMessage();
            String imsi = "unknown";
            if (request != null && request.getMessage() != null) {
                Avp reqUserNameAvp = request.getMessage().getAvps().getAvp(Avp.USER_NAME);
                if (reqUserNameAvp != null) imsi = reqUserNameAvp.getUTF8String();
            }

            String apn = "internet";
            int qci = 9;

            long vendor3gpp = 10415L;
            Avp subDataAvp = msg.getAvps().getAvp(1400, vendor3gpp);
            if (subDataAvp != null) {
                AvpSet subDataGroup = subDataAvp.getGrouped();
                
                // Extract APN from APN-Configuration-Profile -> APN-Configuration -> Service-Selection
                Avp apnConfigProfileAvp = subDataGroup.getAvp(1429, vendor3gpp);
                if (apnConfigProfileAvp != null) {
                    AvpSet apnConfigProfileGroup = apnConfigProfileAvp.getGrouped();
                    Avp apnConfigAvp = apnConfigProfileGroup.getAvp(1430, vendor3gpp);
                    if (apnConfigAvp != null) {
                        AvpSet apnConfigGroup = apnConfigAvp.getGrouped();
                        Avp serviceSelectionAvp = apnConfigGroup.getAvp(493); // Service-Selection (APN string)
                        if (serviceSelectionAvp != null) {
                            apn = serviceSelectionAvp.getUTF8String();
                        }
                        
                        // Extract QCI from QOS-Profile
                        Avp qosInfoAvp = apnConfigGroup.getAvp(1431, vendor3gpp);
                        if (qosInfoAvp != null) {
                            AvpSet qosInfoGroup = qosInfoAvp.getGrouped();
                            Avp qciAvp = qosInfoGroup.getAvp(1028, vendor3gpp);
                            if (qciAvp != null) {
                                qci = qciAvp.getInteger32();
                            }
                        }
                    }
                }
            }

            Log.infof("S6a: ULA Success for IMSI %s. APN: %s, QCI: %d. Firing event.", imsi, apn, qci);
            ulaEvent.fire(new com.github.jonasmelchior.mymme.service.UpdateLocationReceivedEvent(imsi, apn, qci));
        } catch (Exception e) {
            Log.error("Error processing S6a ULA", e);
        }
    }
    @Override
    public void doPurgeUEAnswerEvent(ClientS6aSession session, JPurgeUERequest request, JPurgeUEAnswer answer) {}
    @Override
    public void doInsertSubscriberDataRequestEvent(ClientS6aSession session, JInsertSubscriberDataRequest request) {}
    @Override
    public void doDeleteSubscriberDataRequestEvent(ClientS6aSession session, JDeleteSubscriberDataRequest request) {}
    @Override
    public void doCancelLocationRequestEvent(ClientS6aSession session, JCancelLocationRequest request) {}
    @Override
    public void doResetRequestEvent(ClientS6aSession session, JResetRequest request) {}
    @Override
    public void doNotifyAnswerEvent(ClientS6aSession session, JNotifyRequest request, JNotifyAnswer answer) {}
    @Override
    public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer) {}
}
