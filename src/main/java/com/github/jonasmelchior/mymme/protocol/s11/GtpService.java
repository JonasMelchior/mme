package com.github.jonasmelchior.mymme.protocol.s11;

import com.github.jonasmelchior.mymme.config.GtpConfig;
import com.github.jonasmelchior.mymme.data.UeContext;
import com.github.jonasmelchior.mymme.protocol.s11.codec.GtpDecoder;
import com.github.jonasmelchior.mymme.protocol.s11.codec.GtpEncoder;
import com.github.jonasmelchior.mymme.protocol.s11.model.*;
import com.github.jonasmelchior.mymme.repository.UeRepository;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.datagram.DatagramSocket;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jboss.logging.Logger;

@ApplicationScoped
public class GtpService {

    private static final Logger LOG = Logger.getLogger(GtpService.class);

    @Inject
    com.github.jonasmelchior.mymme.service.EmmService emmService;

    @Inject
    UeRepository ueRepository;

    @Inject
    GtpConfig config;

    @Inject
    Vertx vertx;

    private DatagramSocket socket;
    private final AtomicInteger sequenceNumberGenerator = new AtomicInteger(1);
    private final AtomicInteger teidGenerator = new AtomicInteger(100);

    @PostConstruct
    void init() {
        socket = vertx.createDatagramSocket();
        socket.listen(config.localPort(), config.localIp())
            .subscribe().with(
                s -> {
                    LOG.infof("GTPv2: Listening on %s:%d", config.localIp(), config.localPort());
                    s.handler(packet -> handleIncomingPacket(packet.data().getBytes()));
                },
                e -> LOG.error("GTPv2: Failed to start UDP listener", e)
            );
    }

    private void handleIncomingPacket(byte[] data) {
        try {
            Gtpv2Message message = GtpDecoder.decode(data);
            LOG.infof("GTPv2: Received message type %d, TEID %d, Seq %d", 
                message.getHeader().getMessageType(), 
                message.getHeader().getTeid(), 
                message.getHeader().getSequenceNumber());

            switch (message.getHeader().getMessageType()) {
                case Gtpv2Constants.TYPE_CREATE_SESSION_RESPONSE:
                    handleCreateSessionResponse(message);
                    break;
                default:
                    LOG.warnf("GTPv2: Unsupported message type %d", message.getHeader().getMessageType());
            }
        } catch (Exception e) {
            LOG.error("GTPv2: Failed to decode incoming packet", e);
        }
    }

    public void sendCreateSessionRequest(String imsi) {
        ueRepository.findByImsi(imsi).ifPresent(context -> {
            try {
                int mmeTeid = teidGenerator.getAndIncrement();
                int seq = sequenceNumberGenerator.getAndIncrement();
                context.setMmeS11Teid(mmeTeid);
                ueRepository.save(context);

                LOG.infof("GTPv2: Sending 3GPP compliant Create Session Request for IMSI: %s (MME TEID: %d, Seq: %d)", imsi, mmeTeid, seq);

                Gtpv2Header header = new Gtpv2Header(Gtpv2Constants.TYPE_CREATE_SESSION_REQUEST, 0, seq);
                Gtpv2Message csr = new Gtpv2Message(header);

                csr.addElement(new ImsiIe(imsi, (byte)0));
                csr.addElement(new RatTypeIe((byte)6, (byte)0)); // EUTRAN
                
                String mcc = context.getMcc() != null ? context.getMcc() : "001";
                String mnc = context.getMnc() != null ? context.getMnc() : "01";
                csr.addElement(new ServingNetworkIe(mcc, mnc, (byte)0));
                
                InetAddress localAddr = InetAddress.getByName(config.localIp());
                csr.addElement(new FullyQualifiedTeidIe((byte)10, mmeTeid, localAddr, (byte)0)); // S11 MME GTP-C
                
                csr.addElement(new ApnIe(context.getApn(), (byte)0));
                
                // Real ULI
                csr.addElement(new UliIe(mcc, mnc, context.getTac(), context.getCellId(), (byte)0));

                // Bearer Context
                BearerContextIe bearerContext = new BearerContextIe((byte)0);
                bearerContext.addElement(new EpsBearerIdIe(context.getEbi(), (byte)0));
                bearerContext.addElement(new BearerQosIe((byte)9, (byte)15, false, false, (byte)0));
                csr.addElement(bearerContext);

                byte[] packet = GtpEncoder.encode(csr);
                socket.send(Buffer.buffer(packet), config.sgwPort(), config.sgwIp())
                    .subscribe().with(
                        v -> LOG.debug("GTPv2: CSR sent"),
                        e -> LOG.error("GTPv2: Failed to send CSR", e)
                    );
            } catch (UnknownHostException e) {
                LOG.error("GTPv2: Invalid local IP address", e);
            }
        });
    }

    private void handleCreateSessionResponse(Gtpv2Message message) {
        // In a real MME, we should use the Sequence Number to find the UE context.
        // For now, let's just use the first UE (simplified).
        // Or extract IMSI if present in CSR. CSR usually doesn't have IMSI in response,
        // it uses the TEID (MME TEID we sent).
        
        int mmeTeid = message.getHeader().getTeid();
        ueRepository.findAll().stream()
            .filter(u -> u.getMmeS11Teid() == mmeTeid)
            .findFirst()
            .ifPresentOrElse(context -> {
                LOG.infof("GTPv2: Handling Create Session Response for IMSI: %s", context.getImsi());
                
                message.getElement(CauseIe.class, 0).ifPresent(cause -> {
                    if (cause.getCauseValue() == 16) { // Request Accepted
                        LOG.info("GTPv2: CSR accepted by SGW.");
                        
                        // Extract SGW S11 TEID
                        message.getElement(FullyQualifiedTeidIe.class, 0).ifPresent(fteid -> {
                            context.setSgwS11Teid(fteid.getTeid());
                        });

                        // Extract SGW S1-U F-TEID from Bearer Context
                        message.getElement(BearerContextIe.class, 0).ifPresent(bc -> {
                            bc.getElements().stream()
                                .filter(ie -> ie instanceof FullyQualifiedTeidIe)
                                .map(ie -> (FullyQualifiedTeidIe) ie)
                                .findFirst()
                                .ifPresent(fteid -> {
                                    LOG.infof("GTPv2: Extracted SGW S1-U TEID: %d, IP: %s", fteid.getTeid(), fteid.getIpAddress().getHostAddress());
                                    context.setSgwS1Uteid(fteid.getTeid());
                                    context.setSgwIp(fteid.getIpAddress().getAddress());
                                });
                        });

                        ueRepository.save(context);
                        emmService.onSessionCreated(context.getImsi());
                    } else {
                        LOG.errorf("GTPv2: CSR rejected with cause: %d", cause.getCauseValue());
                    }
                });
            }, () -> LOG.errorf("GTPv2: No UE context found for MME TEID: %d", mmeTeid));
    }
}
