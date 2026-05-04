package com.github.jonasmelchior.mymme.protocol.s1ap;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.sctp.SctpMessage;
import org.jboss.logging.Logger;

public class S1apHandler extends SimpleChannelInboundHandler<SctpMessage> {

    private static final Logger LOG = Logger.getLogger(S1apHandler.class);
    private final EnbManager enbManager;
    private final S1apService s1apService;

    public S1apHandler(EnbManager enbManager, S1apService s1apService) {
        this.enbManager = enbManager;
        this.s1apService = s1apService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("New eNodeB connection established: " + ctx.channel().remoteAddress());
        enbManager.addEnb(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("eNodeB connection closed: " + ctx.channel().remoteAddress());
        enbManager.removeEnb(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SctpMessage msg) throws Exception {
        LOG.debug("Received S1AP message from " + ctx.channel().remoteAddress());
        byte[] data = new byte[msg.content().readableBytes()];
        msg.content().readBytes(data);
        s1apService.handleIncomingMessage(ctx, data);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("Exception in S1AP handler", cause);
        ctx.close();
    }
}
