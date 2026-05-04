package com.github.jonasmelchior.mymme.repository;

import com.github.jonasmelchior.mymme.data.UeContext;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class UeRepository {
    private static final Logger LOG = Logger.getLogger(UeRepository.class);

    private ValueCommands<String, UeContext> commands;
    private final ConcurrentHashMap<String, UeContext> localCache = new ConcurrentHashMap<>();

    public UeRepository(RedisDataSource ds) {
        try {
            this.commands = ds.value(UeContext.class);
            // Verify connection by checking if commands is initialized
            if (this.commands != null) {
                LOG.info("MME REPOSITORY: ATTEMPTING REDIS CONNECTION...");
            }
        } catch (Exception e) {
            LOG.warn("MME REPOSITORY: REDIS NOT REACHABLE. Falling back to in-memory cache.");
            this.commands = null;
        }
    }

    public void save(UeContext context) {
        localCache.put("ue:" + context.getImsi(), context);
        localCache.put("s1ap:" + context.getMmeUeS1apId(), context);
        localCache.put("enb_ue:" + context.getEnbUeS1apId(), context);
        if (context.getMmeS11Teid() != 0) {
            localCache.put("s11:" + context.getMmeS11Teid(), context);
        }
        
        if (commands != null) {
            try {
                commands.set("ue:" + context.getImsi(), context);
                commands.set("s1ap:" + context.getMmeUeS1apId(), context);
                commands.set("enb_ue:" + context.getEnbUeS1apId(), context);
                if (context.getMmeS11Teid() != 0) {
                    commands.set("s11:" + context.getMmeS11Teid(), context);
                }
            } catch (Exception e) {
                // Silently fallback in development
            }
        }
    }

    public java.util.Collection<UeContext> findAll() {
        return localCache.values();
    }

    public Optional<UeContext> findByEnbUeS1apId(int enbUeS1apId) {
        if (commands != null) {
            try {
                return Optional.ofNullable(commands.get("enb_ue:" + enbUeS1apId));
            } catch (Exception e) {
                // fallback
            }
        }
        return Optional.ofNullable(localCache.get("enb_ue:" + enbUeS1apId));
    }

    public Optional<UeContext> findByImsi(String imsi) {
        if (commands != null) {
            try {
                UeContext context = commands.get("ue:" + imsi);
                if (context != null) return Optional.of(context);
            } catch (Exception e) {
                // Silently fallback in development
            }
        }
        return Optional.ofNullable(localCache.get("ue:" + imsi));
    }

    public Optional<UeContext> findByS1apId(long s1apId) {
        if (commands != null) {
            try {
                return Optional.ofNullable(commands.get("s1ap:" + s1apId));
            } catch (Exception e) {
                LOG.error("Redis lookup failed, falling back to local cache", e);
            }
        }
        return Optional.ofNullable(localCache.get("s1ap:" + s1apId));
    }

    public void delete(UeContext context) {
        localCache.remove("ue:" + context.getImsi());
        localCache.remove("s1ap:" + context.getMmeUeS1apId());
        
        if (commands != null) {
            try {
                commands.getdel("ue:" + context.getImsi());
                commands.getdel("s1ap:" + context.getMmeUeS1apId());
            } catch (Exception e) {
                LOG.error("Redis deletion failed", e);
            }
        }
    }
}
