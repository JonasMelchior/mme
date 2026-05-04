package com.github.jonasmelchior.mymme.protocol.s1ap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.SctpChannel;
import io.netty.channel.sctp.SctpChannelOption;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class S1MmeServer {

    private static final Logger LOG = Logger.getLogger(S1MmeServer.class);

    @ConfigProperty(name = "mme.s1ap.port")
    int port;

    @ConfigProperty(name = "mme.s1ap.host")
    String host;

    @jakarta.inject.Inject
    EnbManager enbManager;

    @jakarta.inject.Inject
    S1apService s1apService;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Starting S1-MME SCTP Server on " + host + ":" + port);
        
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioSctpServerChannel.class)
             .option(SctpChannelOption.SCTP_NODELAY, true)
             .childOption(SctpChannelOption.SCTP_NODELAY, true)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SctpChannel>() {
                 @Override
                 public void initChannel(SctpChannel ch) {
                     ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                     ch.pipeline().addLast(new S1apHandler(enbManager, s1apService));
                 }
             });

            ChannelFuture f = b.bind(host, port).sync();
            LOG.info("S1-MME SCTP Server started successfully");
        } catch (Exception e) {
            LOG.error("Failed to start S1-MME SCTP Server. Ensure SCTP is supported by your OS.", e);
            stop();
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        stop();
    }

    private void stop() {
        LOG.info("Stopping S1-MME SCTP Server");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
