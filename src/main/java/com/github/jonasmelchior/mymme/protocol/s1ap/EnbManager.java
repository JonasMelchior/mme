package com.github.jonasmelchior.mymme.protocol.s1ap;

import io.netty.channel.Channel;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EnbManager {

    private static final Logger LOG = Logger.getLogger(EnbManager.class);

    // Map of Remote Address to Netty Channel
    private final Map<SocketAddress, Channel> activeEnbs = new ConcurrentHashMap<>();

    public void addEnb(Channel channel) {
        LOG.info("Registering eNodeB: " + channel.remoteAddress());
        activeEnbs.put(channel.remoteAddress(), channel);
    }

    public void removeEnb(Channel channel) {
        LOG.info("Unregistering eNodeB: " + channel.remoteAddress());
        activeEnbs.remove(channel.remoteAddress());
    }

    public Channel getEnbChannel(SocketAddress address) {
        return activeEnbs.get(address);
    }

    public int getConnectedEnbCount() {
        return activeEnbs.size();
    }
}
